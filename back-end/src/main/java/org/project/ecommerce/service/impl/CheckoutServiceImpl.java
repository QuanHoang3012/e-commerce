package org.project.ecommerce.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.project.ecommerce.constant.OrderStatus;
import org.project.ecommerce.dto.request.CheckoutRequest;
import org.project.ecommerce.dto.response.CheckoutResponse;
import org.project.ecommerce.entities.Cart;
import org.project.ecommerce.entities.CartItem;
import org.project.ecommerce.entities.CheckoutSession;
import org.project.ecommerce.entities.Order;
import org.project.ecommerce.entities.OrderItem;
import org.project.ecommerce.exception.CustomException;
import org.project.ecommerce.repository.CartRepository;
import org.project.ecommerce.repository.CheckoutSessionRepository;
import org.project.ecommerce.repository.OrderRepository;
import org.project.ecommerce.service.CheckoutService;
import org.project.ecommerce.service.EmailService;
import org.project.ecommerce.service.InventoryReservationService;
import org.project.ecommerce.utils.OrderUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutServiceImpl implements CheckoutService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final CheckoutSessionRepository checkoutSessionRepository;
    private final InventoryReservationService reservationService;
    private final EmailService emailService;

    private static final int RESERVATION_EXPIRY_MINUTES = 15;

    @Override
    @Transactional
    public CheckoutResponse initiateCheckout(UUID cartId) {
        // 1. Lấy giỏ hàng
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CustomException("Giỏ hàng không tồn tại", HttpStatus.NOT_FOUND.value()));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new CustomException("Giỏ hàng trống", HttpStatus.BAD_REQUEST.value());
        }

        // 2. Gen sessionId mới (độc lập với cartId)
        String sessionId = UUID.randomUUID().toString();

        // 3. Tạo Order ngay với snapshot data từ cart
        BigDecimal totalAmount = cart.getItems().stream()
                .map(item -> item.getVariant().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        // Copy cart items sang order items (snapshot)
        for (CartItem cartItem : cart.getItems()) {
            BigDecimal itemSubtotal = cartItem.getVariant().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .variant(cartItem.getVariant())
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(cartItem.getVariant().getPrice())
                    .subtotal(itemSubtotal)
                    .build();
            order.getItems().add(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        // 4. Tạo CheckoutSession để track
        Instant expiresAt = Instant.now().plus(RESERVATION_EXPIRY_MINUTES, ChronoUnit.MINUTES);
        CheckoutSession session = CheckoutSession.builder()
                .sessionId(sessionId)
                .cart(cart)
                .order(savedOrder)
                .expiresAt(expiresAt)
                .isCompleted(false)
                .build();
        checkoutSessionRepository.save(session);

        // 5. Reserve inventory dựa trên ORDER ITEMS với sessionId mới
        List<InventoryReservationService.ReservationItem> reservationItems = savedOrder.getItems().stream()
                .map(item -> new InventoryReservationService.ReservationItem(
                        item.getVariant().getId(),
                        item.getQuantity()))
                .collect(Collectors.toList());

        try {
            reservationService.reserveMultipleItems(reservationItems, sessionId);
        } catch (CustomException e) {
            // Rollback: xóa session và order
            checkoutSessionRepository.delete(session);
            orderRepository.delete(savedOrder);

            throw new CustomException(
                    "Không thể đặt hàng: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST.value());
        }

        log.info("Checkout initiated - Session {} - Cart {} - Order {} - {} items - total {}",
                sessionId, cartId, savedOrder.getId(), savedOrder.getItems().size(), totalAmount);

        return CheckoutResponse.builder()
                .sessionId(sessionId)
                .reservedUntil(expiresAt)
                .totalItems(savedOrder.getItems().size())
                .totalAmount(totalAmount)
                .message("Đã giữ hàng cho bạn trong " + RESERVATION_EXPIRY_MINUTES
                        + " phút. Vui lòng hoàn tất thanh toán!")
                .build();
    }

    @Override
    @Transactional
    public CheckoutResponse confirmCheckout(String sessionId, CheckoutRequest request) {
        // 1. Lấy checkout session
        CheckoutSession session = checkoutSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new CustomException("Session không tồn tại", HttpStatus.NOT_FOUND.value()));

        if (Boolean.TRUE.equals(session.getIsCompleted())) {
            throw new CustomException("Session đã được sử dụng", HttpStatus.BAD_REQUEST.value());
        }

        // 2. Lấy Order đã tạo sẵn từ session
        Order order = session.getOrder();

        // 3. Cập nhật thông tin khách hàng vào Order
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());

        // 4. Xác định trạng thái theo payment method
        OrderStatus finalStatus = OrderUtils.determineInitialStatus(request.getPaymentMethod());
        order.setStatus(finalStatus);

        // 5. Complete reservation → Trừ stock thật sự
        try {
            reservationService.completeReservation(sessionId);
        } catch (CustomException e) {
            throw new CustomException(
                    "Lỗi khi hoàn tất đơn hàng: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        // 6. Lưu Order và đánh dấu session completed
        Order savedOrder = orderRepository.save(order);
        session.setIsCompleted(true);
        checkoutSessionRepository.save(session);

        // 7. Clear cart items sau khi checkout thành công
        Cart cart = session.getCart();
        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Order confirmed - Session {} - Order ID: {} - Total: {} - Payment: {} - Cart cleared",
                sessionId, savedOrder.getId(), savedOrder.getTotalAmount(), request.getPaymentMethod());

        // 8. Gửi email xác nhận
        emailService.sendOrderConfirmation(savedOrder);

        // 9. Tạo message phù hợp với payment method
        String message = OrderUtils.buildSuccessMessage(savedOrder, request.getPaymentMethod());

        return CheckoutResponse.builder()
                .orderId(savedOrder.getId())
                .totalAmount(savedOrder.getTotalAmount())
                .message(message)
                .build();
    }

    @Override
    @Transactional
    public void cancelCheckout(String sessionId) {
        // Lấy session và validate
        CheckoutSession session = checkoutSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new CustomException("Session không tồn tại", HttpStatus.NOT_FOUND.value()));

        if (Boolean.TRUE.equals(session.getIsCompleted())) {
            throw new CustomException("Session đã hoàn tất, không thể hủy", HttpStatus.BAD_REQUEST.value());
        }

        // Release reservation
        reservationService.releaseReservation(sessionId);

        // Xóa order và session
        if (session.getOrder() != null) {
            orderRepository.delete(session.getOrder());
        }
        checkoutSessionRepository.delete(session);

        log.info("Checkout cancelled for session {}", sessionId);
    }
}
