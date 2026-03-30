package org.project.ecommerce.service;

import java.util.UUID;

import org.project.ecommerce.constant.OrderStatus;
import org.project.ecommerce.dto.PageDTO;
import org.project.ecommerce.dto.response.OrderListResponse;
import org.project.ecommerce.dto.response.OrderTrackingResponse;
import org.springframework.data.domain.Pageable;

/**
 * Service xử lý đơn hàng
 */
public interface OrderService {
    /**
     * Cleanup các orders đã hết hạn (status=PENDING và createdAt > 15 phút)
     * → Release reservations
     * → Xóa orders
     *
     * @return Số lượng orders đã cleanup
     */
    int cleanupExpiredOrders();
    /**
     * Tracking đơn hàng bằng order ID (PUBLIC - không cần auth)
     * 
     * @param orderId ID của đơn hàng
     * @return Chi tiết đơn hàng và timeline
     */
    OrderTrackingResponse trackOrderById(UUID orderId);

    /**
     * Lấy danh sách đơn hàng (dành cho admin/warehouse)
     * Hỗ trợ filter theo status và phân trang
     * 
     * @param status Filter theo trạng thái (optional)
     * @param pageable Phân trang
     * @return Danh sách đơn hàng
     */
    PageDTO<OrderListResponse> getOrders(OrderStatus status, Pageable pageable);

    /**
     * Đổi trạng thái đơn hàng (dành cho warehouse staff)
     * 
     * @param orderId ID đơn hàng
     * @param newStatus Trạng thái mới
     * @param note Ghi chú (optional)
     * @return Chi tiết đơn hàng sau khi update
     */
    OrderTrackingResponse updateOrderStatus(UUID orderId, OrderStatus newStatus, String note);
}
