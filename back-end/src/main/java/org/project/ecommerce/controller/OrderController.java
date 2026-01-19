package org.project.ecommerce.controller;

import java.util.UUID;

import org.project.ecommerce.base.BaseController;
import org.project.ecommerce.base.BaseResponse;
import org.project.ecommerce.config.authentication.RequireApiKey;
import org.project.ecommerce.constant.OrderStatus;
import org.project.ecommerce.dto.PageDTO;
import org.project.ecommerce.dto.request.UpdateOrderStatusRequest;
import org.project.ecommerce.dto.response.OrderListResponse;
import org.project.ecommerce.dto.response.OrderTrackingResponse;
import org.project.ecommerce.service.OrderService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "API quản lý đơn hàng")
public class OrderController extends BaseController {

    private final OrderService orderService;

    /**
     * Lấy danh sách đơn hàng (dành cho warehouse staff)
     * GET /api/v1/orders?status=PENDING&page=1&size=20&sort=createdAt,desc
     * Yêu cầu: Header X-Admin-Key
     */
    @RequireApiKey
    @GetMapping
    public BaseResponse<PageDTO<OrderListResponse>> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort
    ) {
        int pageNo = (page < 1) ? 0 : page - 1;
        Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
        Pageable pageable = PageRequest.of(pageNo, size, sorting);

        PageDTO<OrderListResponse> orders = orderService.getOrders(status, pageable);
        return wrapSuccess(orders);
    }

    /**
     * Tracking đơn hàng bằng Order ID (PUBLIC - không cần auth)
     * GET /api/v1/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public BaseResponse<OrderTrackingResponse> trackOrder(@PathVariable UUID orderId) {
        return wrapSuccess(orderService.trackOrderById(orderId));
    }

    /**
     * Đổi trạng thái đơn hàng (dành cho warehouse staff)
     * Yêu cầu: Header X-Admin-Key
     */
    @RequireApiKey
    @PutMapping("/{orderId}/status")
    public BaseResponse<OrderTrackingResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        return wrapSuccess(orderService.updateOrderStatus(
                orderId, 
                request.getStatus(), 
                request.getNote()
        ));
    }
}

