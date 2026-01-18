package org.project.ecommerce.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.project.ecommerce.constant.OrderStatus;
import org.project.ecommerce.dto.response.OrderTrackingResponse;
import org.project.ecommerce.dto.response.OrderTrackingResponse.OrderItemResponse;
import org.project.ecommerce.dto.response.OrderTrackingResponse.OrderTimeline;
import org.project.ecommerce.dto.response.OrderTrackingResponse.TimelineStep;
import org.project.ecommerce.entities.Order;
import org.project.ecommerce.entities.OrderItem;
import org.project.ecommerce.exception.CustomException;
import org.project.ecommerce.repository.OrderRepository;
import org.project.ecommerce.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public OrderTrackingResponse trackOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(
                        "Không tìm thấy đơn hàng", 
                        HttpStatus.NOT_FOUND.value()));

        return buildOrderTrackingResponse(order);
    }

    /**
     * Build response từ Order entity
     */
    private OrderTrackingResponse buildOrderTrackingResponse(Order order) {
        // Build order items
        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::buildOrderItemResponse)
                .collect(Collectors.toList());

        // Build timeline
        OrderTimeline timeline = buildOrderTimeline(order);

        return OrderTrackingResponse.builder()
                .orderId(order.getId())
                .trackingNumber(order.getTrackingNumber())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .customerEmail(order.getCustomerEmail())
                .shippingAddress(order.getShippingAddress())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus())
                .orderDate(order.getCreatedAt())
                .lastUpdated(order.getUpdatedAt())
                .items(items)
                .timeline(timeline)
                .build();
    }

    /**
     * Build response cho từng order item
     */
    private OrderItemResponse buildOrderItemResponse(OrderItem item) {
        String variantInfo = buildVariantInfo(item.getVariant().getSize(), item.getVariant().getColor());

        return OrderItemResponse.builder()
                .productName(item.getVariant().getProduct().getName())
                .variantInfo(variantInfo)
                .skuCode(item.getVariant().getSkuCode())
                .quantity(item.getQuantity())
                .price(item.getPriceAtPurchase())
                .subtotal(item.getSubtotal())
                .build();
    }

    /**
     * Build thông tin variant (Size, Color)
     */
    private String buildVariantInfo(String size, String color) {
        if (size != null && color != null) {
            return String.format("Size: %s, Color: %s", size, color);
        } else if (size != null) {
            return "Size: " + size;
        } else if (color != null) {
            return "Color: " + color;
        }
        return null;
    }

    /**
     * Build timeline dựa trên order status và timestamps
     */
    private OrderTimeline buildOrderTimeline(Order order) {
        OrderStatus currentStatus = order.getStatus();
        Instant createdAt = order.getCreatedAt();
        Instant updatedAt = order.getUpdatedAt();

        return OrderTimeline.builder()
                .pending(buildTimelineStep(OrderStatus.PENDING, currentStatus, createdAt, "Đơn hàng đã được tạo"))
                .confirmed(buildTimelineStep(OrderStatus.CONFIRMED, currentStatus, updatedAt, "Đơn hàng đã được xác nhận"))
                .paid(buildTimelineStep(OrderStatus.PAID, currentStatus, updatedAt, "Thanh toán thành công"))
                .processing(buildTimelineStep(OrderStatus.PROCESSING, currentStatus, updatedAt, "Đang chuẩn bị hàng"))
                .shipping(buildTimelineStep(OrderStatus.SHIPPING, currentStatus, updatedAt, "Đang giao hàng"))
                .delivered(buildTimelineStep(OrderStatus.DELIVERED, currentStatus, updatedAt, "Đã giao hàng"))
                .completed(buildTimelineStep(OrderStatus.COMPLETED, currentStatus, updatedAt, "Hoàn tất"))
                .build();
    }

    /**
     * Build một timeline step
     * Completed = true nếu order status >= step status
     */
    private TimelineStep buildTimelineStep(OrderStatus stepStatus, OrderStatus currentStatus, 
                                          Instant timestamp, String note) {
        boolean completed = isStatusReached(stepStatus, currentStatus);
        
        return TimelineStep.builder()
                .completed(completed)
                .timestamp(completed ? timestamp : null)
                .note(completed ? note : null)
                .build();
    }

    /**
     * Kiểm tra xem order đã đạt đến status nào chưa
     * Dựa trên thứ tự: PENDING < CONFIRMED < PAID < PROCESSING < SHIPPING < DELIVERED < COMPLETED
     */
    private boolean isStatusReached(OrderStatus targetStatus, OrderStatus currentStatus) {
        int targetOrder = getStatusOrder(targetStatus);
        int currentOrder = getStatusOrder(currentStatus);
        return currentOrder >= targetOrder;
    }

    /**
     * Get order number của status để so sánh
     */
    private int getStatusOrder(OrderStatus status) {
        switch (status) {
            case PENDING:
                return 1;
            case CONFIRMED:
                return 2;
            case PAID:
                return 3;
            case PROCESSING:
                return 4;
            case SHIPPING:
                return 5;
            case DELIVERED:
                return 6;
            case COMPLETED:
                return 7;
            case CANCELLED:
            default:
                return 0;
        }
    }
}
