package org.project.ecommerce.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.project.ecommerce.constant.OrderStatus;
import org.project.ecommerce.constant.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response cho danh sách đơn hàng (dành cho admin/warehouse)
 * Compact hơn OrderTrackingResponse, chỉ hiển thị info cần thiết
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListResponse {
    
    private UUID orderId;
    
    // Thông tin khách hàng
    private String customerName;
    private String customerPhone;
    private String shippingAddress;
    
    // Thông tin đơn hàng
    private BigDecimal totalAmount;
    private PaymentMethod paymentMethod;
    private OrderStatus status;
    
    // Thời gian
    private Instant orderDate;
    private Instant lastUpdated;
    
    // Tổng số items
    private Integer totalItems;
}
