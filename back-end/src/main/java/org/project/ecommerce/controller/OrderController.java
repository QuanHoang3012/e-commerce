package org.project.ecommerce.controller;

import java.util.UUID;

import org.project.ecommerce.base.BaseController;
import org.project.ecommerce.base.BaseResponse;
import org.project.ecommerce.dto.response.OrderTrackingResponse;
import org.project.ecommerce.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Controller theo dõi đơn hàng
 * PUBLIC - KHÔNG CẦN AUTHENTICATION
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "API theo dõi đơn hàng công khai")
public class OrderController extends BaseController {

    private final OrderService orderService;

    /**
     * Tracking đơn hàng bằng Order ID
     * GET /api/v1/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public BaseResponse<OrderTrackingResponse> trackOrder(@PathVariable UUID orderId) {
        return wrapSuccess(orderService.trackOrderById(orderId));
    }
}

