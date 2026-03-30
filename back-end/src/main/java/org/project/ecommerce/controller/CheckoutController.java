package org.project.ecommerce.controller;

import java.util.UUID;

import org.project.ecommerce.base.BaseController;
import org.project.ecommerce.base.BaseResponse;
import org.project.ecommerce.dto.request.CheckoutRequest;
import org.project.ecommerce.dto.response.CheckoutResponse;
import org.project.ecommerce.service.CheckoutService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller xử lý checkout process với inventory reservation
 */
@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Checkout", description = "API checkout - Khởi tạo và hoàn tất thanh toán")
public class CheckoutController extends BaseController {

    private final CheckoutService checkoutService;

    /**
     * Bước 1: Khách hàng bấm "Thanh toán" từ giỏ hàng
     * POST /api/checkout/initiate?cartId={cartId}
     * → Tạo Order PENDING, reserve inventory trong 15 phút
     *
     * Response:
     * {
     *   "orderId": "...",
     *   "reservedUntil": "2026-01-21T10:15:00Z",
     *   "totalItems": 3,
     *   "totalAmount": 1500000
     * }
     */
    @PostMapping("/initiate")
    public BaseResponse<CheckoutResponse> initiateCheckout(
            @RequestParam UUID cartId
    ) {
        log.info("Initiating checkout for cart {}", cartId);

        CheckoutResponse response = checkoutService.initiateCheckout(cartId);

        return wrapSuccess(response);
    }

    /**
     * Bước 2: Khách hàng điền thông tin và xác nhận thanh toán
     * POST /api/checkout/confirm?orderId={orderId}
     * Body: { customerName, customerPhone, customerEmail, shippingAddress, paymentMethod }
     * → Update Order (PENDING → CONFIRMED), complete reservation, trừ stock
     *
     * Response:
     * {
     *   "orderId": "...",
     *   "totalAmount": 1500000,
     *   "message": "Đặt hàng thành công!"
     * }
     */
    @PostMapping("/confirm")
    public BaseResponse<CheckoutResponse> confirmCheckout(
            @RequestParam UUID orderId,
            @Valid @RequestBody CheckoutRequest request
    ) {
        log.info("Confirming checkout for order {}", orderId);

        CheckoutResponse response = checkoutService.confirmCheckout(orderId, request);

        return wrapSuccess(response);
    }

    /**
     * Khách hàng cancel checkout
     * POST /api/checkout/cancel?orderId={orderId}
     * → Release reservation, xóa Order PENDING
     */
    @PostMapping("/cancel")
    public BaseResponse<Void> cancelCheckout(
            @RequestParam UUID orderId
    ) {
        log.info("Cancelling checkout for order {}", orderId);

        checkoutService.cancelCheckout(orderId);

        return wrapSuccess(null);
    }
}
