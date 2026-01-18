package org.project.ecommerce.dto.request;

import org.project.ecommerce.constant.OrderStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {
    
    @NotNull(message = "Trạng thái không được để trống")
    private OrderStatus status;
    
    /**
     * Ghi chú khi đổi trạng thái (optional)
     * VD: "Khách yêu cầu hủy", "Đã giao cho shipper ABC"
     */
    private String note;
}
