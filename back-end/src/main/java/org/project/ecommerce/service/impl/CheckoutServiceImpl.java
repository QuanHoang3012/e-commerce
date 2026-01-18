package org.project.ecommerce.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.project.ecommerce.constant.CartStatus;
import org.project.ecommerce.constant.OrderStatus;
import org.project.ecommerce.dto.request.CheckoutRequest;
import org.project.ecommerce.utils.OrderUtils;
import org.project.ecommerce.dto.response.CheckoutResponse;
import org.project.ecommerce.entities.Cart;
import org.project.ecommerce.entities.CartItem;
import org.project.ecommerce.entities.Order;
import org.project.ecommerce.entities.OrderItem;
import org.project.ecommerce.exception.CustomException;
import org.project.ecommerce.repository.CartRepository;
import org.project.ecommerce.repository.OrderRepository;
import org.project.ecommerce.service.CheckoutService;
import org.project.ecommerce.service.EmailService;
import org.project.ecommerce.service.InventoryReservationService;
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

        // 2. Tạo session ID (dùng cartId.toString() hoặc UUID mới)
        String sessionId = cartId.toString();

        // 3. Reserve inventory cho từng item trong giỏ
        List<InventoryReservationService.ReservationItem> reservationItems = cart.getItems().stream()
                .map(item -> new InventoryReservationService.ReservationItem(
                        item.getVariant().getId(),
                        item.getQuantity()))
                .collect(Collectors.toList());

        try {
            reservationService.reserveMultipleItems(reservationItems, sessionId);
        } catch (CustomException e) {
            // Nếu không đủ hàng → throw lỗi rõ ràng cho client
            throw new CustomException(
                    "Không thể đặt hàng: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST.value());
        }

        // 4. Cập nhật trạng thái giỏ hàng
        cart.setStatus(CartStatus.CHECKOUT);
        cartRepository.save(cart);

        // 5. Tính tổng tiền
        BigDecimal totalAmount = cart.getItems().stream()
                .map(item -> item.getVariant().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Instant reservedUntil = Instant.now().plus(RESERVATION_EXPIRY_MINUTES, ChronoUnit.MINUTES);

        log.info("Checkout initiated for cart {} - session {} - {} items - total {}",
                cartId, sessionId, cart.getItems().size(), totalAmount);

        return CheckoutResponse.builder()
                .sessionId(sessionId)
                .reservedUntil(reservedUntil)
                .totalItems(cart.getItems().size())
                .totalAmount(totalAmount)
                .message("Đã giữ hàng cho bạn trong " + RESERVATION_EXPIRY_MINUTES
                        + " phút. Vui lòng hoàn tất thanh toán!")
                .build();
    }

    @Override
    @Transactional
    public CheckoutResponse confirmCheckout(String sessionId, CheckoutRequest request) {
        // 1. Lấy giỏ hàng từ sessionId (sessionId = cartId)
        UUID cartId;
        try {
            cartId = UUID.fromString(sessionId);
        } catch (IllegalArgumentException e) {
            throw new CustomException("Session không hợp lệ", HttpStatus.BAD_REQUEST.value());
        }

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CustomException("Giỏ hàng không tồn tại", HttpStatus.NOT_FOUND.value()));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new CustomException("Giỏ hàng trống", HttpStatus.BAD_REQUEST.value());
        }

        // 2. Tính tổng tiền
        BigDecimal totalAmount = cart.getItems().stream()
                .map(item -> item.getVariant().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Xác định trạng thái ban đầu theo payment method
        OrderStatus initialStatus = OrderUtils.determineInitialStatus(request.getPaymentMethod());

        // 4. Tạo Order
        Order order = Order.builder()
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerEmail(request.getCustomerEmail())
                .shippingAddress(request.getShippingAddress())
                .totalAmount(totalAmount)
                .paymentMethod(request.getPaymentMethod())
                .status(initialStatus)
                .items(new ArrayList<>())
                .build();

        // 5. Tạo OrderItems từ CartItems
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

        // 5. Complete reservation → Trừ stock thật sự
        try {
            reservationService.completeReservation(sessionId);
        } catch (CustomException e) {
            throw new CustomException(
                    "Lỗi khi hoàn tất đơn hàng: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        // 6. Lưu Order
        Order savedOrder = orderRepository.save(order);

        // 7. Xóa giỏ hàng (hoặc đánh dấu COMPLETED)
        cart.setStatus(CartStatus.COMPLETED);
        cart.getItems().clear(); // Xóa items
        cartRepository.save(cart);

        log.info("Order created successfully - Order ID: {} - Total: {} - Payment: {}",
                savedOrder.getId(), totalAmount, request.getPaymentMethod());

        // 8. Gửi email xác nhận
        emailService.sendOrderConfirmation(savedOrder);

        // 9. Tạo message phù hợp với payment method
        String message = OrderUtils.buildSuccessMessage(savedOrder, request.getPaymentMethod());

        return CheckoutResponse.builder()
                .orderId(savedOrder.getId())
                .totalAmount(totalAmount)
                .message(message)
                .build();
    }

    @Override
    @Transactional
    public void cancelCheckout(String sessionId) {
        // Release reservation
        reservationService.releaseReservation(sessionId);

        // Cập nhật lại trạng thái giỏ hàng về ACTIVE (nếu cần)
        try {
            UUID cartId = UUID.fromString(sessionId);
            Cart cart = cartRepository.findById(cartId).orElse(null);
            if (cart != null && cart.getStatus() == CartStatus.CHECKOUT) {
                cart.setStatus(CartStatus.ACTIVE);
                cartRepository.save(cart);
            }
        } catch (IllegalArgumentException e) {
            // Ignore nếu sessionId không phải UUID
        }

        log.info("Checkout cancelled for session {}", sessionId);
    }
}
