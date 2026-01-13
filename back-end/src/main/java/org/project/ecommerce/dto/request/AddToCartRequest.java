package org.project.ecommerce.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class AddToCartRequest {
    private UUID variantId; // Khách chọn cái SKU nào (Size/Màu cụ thể)
    private int quantity;   // Số lượng muốn mua
}
