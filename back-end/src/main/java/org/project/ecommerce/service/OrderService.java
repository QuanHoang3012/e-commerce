package org.project.ecommerce.service;

import java.util.UUID;

import org.project.ecommerce.dto.response.OrderTrackingResponse;

/**
 * Service xử lý tracking đơn hàng
 * KHÔNG CẦN ĐĂNG NHẬP - chỉ cần order ID
 */
public interface OrderService {

    /**
     * Tracking đơn hàng bằng order ID
     * Endpoint public - không cần authentication
     * 
     * @param orderId ID của đơn hàng
     * @return Chi tiết đơn hàng và timeline
     */
    OrderTrackingResponse trackOrderById(UUID orderId);
}
