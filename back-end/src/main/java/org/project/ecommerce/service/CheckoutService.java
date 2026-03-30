package org.project.ecommerce.service;

import java.util.UUID;

import org.project.ecommerce.dto.request.CheckoutRequest;
import org.project.ecommerce.dto.response.CheckoutResponse;

/**
 * Service xử lý checkout flow với inventory reservation
 */
public interface CheckoutService {

    /**
     * Bước 1: Khách hàng bấm "Thanh toán" từ giỏ hàng
     * → Tạo Order với status=PENDING (chưa có thông tin khách)
     * → Reserve inventory cho các items trong giỏ
     *
     * @param cartId ID của giỏ hàng
     * @return Order ID để thanh toán
     */
    CheckoutResponse initiateCheckout(UUID cartId);

    /**
     * Bước 2: Khách hàng điền thông tin và xác nhận thanh toán
     * → Update Order (thêm thông tin khách hàng, status=CONFIRMED)
     * → Complete reservation
     * → Trừ stock_quantity thật sự
     * → Xóa giỏ hàng
     *
     * @param orderId Order ID từ bước 1
     * @param request Thông tin thanh toán (địa chỉ, SĐT, payment method...)
     * @return Order đã hoàn tất
     */
    CheckoutResponse confirmCheckout(UUID orderId, CheckoutRequest request);

    /**
     * Khách hàng cancel giữa chừng
     * → Release reservation
     * → Xóa Order PENDING
     *
     * @param orderId Order ID cần cancel
     */
    void cancelCheckout(UUID orderId);

}
