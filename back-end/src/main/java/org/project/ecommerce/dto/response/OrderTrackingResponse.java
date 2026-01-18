package org.project.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.project.ecommerce.constant.OrderStatus;
import org.project.ecommerce.constant.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingResponse {

    private UUID orderId;
    private String trackingNumber;
    
    // Thông tin khách hàng
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String shippingAddress;
    
    // Thông tin đơn hàng
    private BigDecimal totalAmount;
    private PaymentMethod paymentMethod;
    private OrderStatus status;
    
    // Timeline
    private Instant orderDate;
    private Instant lastUpdated;
    
    // Chi tiết sản phẩm
    private List<OrderItemResponse> items;
    
    // Trạng thái timeline
    private OrderTimeline timeline;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private String productName;
        private String variantInfo;  // Size, Color
        private String skuCode;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderTimeline {
        private TimelineStep pending;
        private TimelineStep confirmed;
        private TimelineStep paid;
        private TimelineStep processing;
        private TimelineStep shipping;
        private TimelineStep delivered;
        private TimelineStep completed;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineStep {
        private boolean completed;
        private Instant timestamp;
        private String note;
    }
}
