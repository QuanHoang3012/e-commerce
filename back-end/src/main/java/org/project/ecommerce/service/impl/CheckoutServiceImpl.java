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
import org.project.ecommerce.entities.Order;
import org.project.ecommerce.entities.OrderItem;
import org.project.ecommerce.exception.CustomException;
import org.project.ecommerce.repository.CartRepository;
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

        // 2. Tính tổng tiền
        BigDecimal totalAmount = cart.getItems().stream()
                .map(item -> item.getVariant().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Tạo Order với status=PENDING, chưa có thông tin khách hàng
        Order order = Order.builder()
                .cart(cart)  // Link cart để snapshot items
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        // 4. Copy cart items sang order items (snapshot data)
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

        // 5. Reserve inventory dựa trên ORDER ITEMS
        List<InventoryReservationService.ReservationItem> reservationItems = savedOrder.getItems().stream()
                .map(item -> new InventoryReservationService.ReservationItem(
                        item.getVariant().getId(),
                        item.getQuantity()))
                .collect(Collectors.toList());

        try {
            reservationService.reserveMultipleItems(reservationItems, savedOrder.getId());
        } catch (CustomException e) {
            // Rollback: xóa order
            orderRepository.delete(savedOrder);

            throw new CustomException(
                    "Không thể đặt hàng: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST.value());
        }

        log.info("Checkout initiated - Order {} - Cart {} - {} items - total {}",
                savedOrder.getId(), cartId, savedOrder.getItems().size(), totalAmount);

        // Tính expires time
        Instant expiresAt = Instant.now().plus(RESERVATION_EXPIRY_MINUTES, ChronoUnit.MINUTES);

        return CheckoutResponse.builder()
                .orderId(savedOrder.getId())
                .reservedUntil(expiresAt)
                .totalItems(savedOrder.getItems().size())
                .totalAmount(totalAmount)
                .message("Đã giữ hàng cho bạn trong " + RESERVATION_EXPIRY_MINUTES
                        + " phút. Vui lòng hoàn tất thanh toán!")
                .build();
    }

    @Override
    @Transactional
    public CheckoutResponse confirmCheckout(UUID orderId, CheckoutRequest request) {
        // 1. Lấy order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Đơn hàng không tồn tại", HttpStatus.NOT_FOUND.value()));

        // 2. Validate status PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new CustomException("Đơn hàng đã được xác nhận", HttpStatus.BAD_REQUEST.value());
        }

        // 3. Validate thời gian (không quá 15 phút)
        Instant expiryTime = order.getCreatedAt().plus(RESERVATION_EXPIRY_MINUTES, ChronoUnit.MINUTES);
        if (Instant.now().isAfter(expiryTime)) {
            // XÓA reservation TRƯỚC khi xóa order
            try {
                reservationService.deleteReservationsByOrder(orderId);
            } catch (Exception e) {
                log.warn("Error deleting reservation for expired order {}", orderId, e);
            }
            orderRepository.delete(order);
            throw new CustomException("Đơn hàng đã hết hạn", HttpStatus.BAD_REQUEST.value());
        }

        // 4. Cập nhật thông tin khách hàng
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());

        // 5. Xác định trạng thái theo payment method
        OrderStatus finalStatus = OrderUtils.determineInitialStatus(request.getPaymentMethod());
        order.setStatus(finalStatus);

        // 6. Complete reservation → Trừ stock thật sự
        try {
            reservationService.completeReservation(orderId);
        } catch (CustomException e) {
            throw new CustomException(
                    "Lỗi khi hoàn tất đơn hàng: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        // 7. Lưu Order
        Order savedOrder = orderRepository.save(order);

        // 8. Clear cart items sau khi checkout thành công
        Cart cart = order.getCart();
        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Order confirmed - Order ID: {} - Total: {} - Payment: {} - Cart cleared",
                savedOrder.getId(), savedOrder.getTotalAmount(), request.getPaymentMethod());

        // 9. Gửi email xác nhận
        emailService.sendOrderConfirmation(savedOrder);

        // 10. Tạo message phù hợp với payment method
        String message = OrderUtils.buildSuccessMessage(savedOrder, request.getPaymentMethod());

        return CheckoutResponse.builder()
                .orderId(savedOrder.getId())
                .totalAmount(savedOrder.getTotalAmount())
                .message(message)
                .build();
    }

    @Override
    @Transactional
    public void cancelCheckout(UUID orderId) {
        // Lấy order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Đơn hàng không tồn tại", HttpStatus.NOT_FOUND.value()));

        // Validate status PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new CustomException("Đơn hàng đã hoàn tất, không thể hủy", HttpStatus.BAD_REQUEST.value());
        }

        // XÓA reservation TRƯỚC khi xóa order
        try {
            reservationService.deleteReservationsByOrder(orderId);
        } catch (Exception e) {
            log.warn("Error deleting reservation for order {}", orderId, e);
        }

        // Xóa order
        orderRepository.delete(order);

        log.info("Checkout cancelled for order {}", orderId);
    }


}
