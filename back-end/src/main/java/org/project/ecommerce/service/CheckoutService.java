package org.project.ecommerce.service;

import org.project.ecommerce.dto.request.CheckoutRequest;
import org.project.ecommerce.dto.response.CheckoutResponse;

import java.util.UUID;

/**
 * Service xử lý checkout flow với inventory reservation
 */
public interface CheckoutService {

    /**
     * Bước 1: Khách hàng bấm "Thanh toán" từ giỏ hàng
     * → Reserve inventory cho các items trong giỏ
     * → Tạo session checkout (giữ hàng trong 15 phút)
     *
     * @param cartId ID của giỏ hàng
     * @return CheckoutSession với thông tin để thanh toán
     */
    CheckoutResponse initiateCheckout(UUID cartId);

    /**
     * Bước 2: Khách hàng điền thông tin và xác nhận thanh toán
     * → Complete reservation
     * → Tạo Order
     * → Trừ stock_quantity thật sự
     * → Xóa giỏ hàng
     *
     * @param sessionId Session ID từ bước 1
     * @param request   Thông tin thanh toán (địa chỉ, SĐT, payment method...)
     * @return Order đã tạo thành công
     */
    CheckoutResponse confirmCheckout(String sessionId, CheckoutRequest request);

    /**
     * Khách hàng cancel giữa chừng
     * → Release reservation
     *
     * @param sessionId Session ID cần cancel
     */
    void cancelCheckout(String sessionId);
}
