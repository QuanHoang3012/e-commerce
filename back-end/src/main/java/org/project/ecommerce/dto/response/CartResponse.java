package org.project.ecommerce.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CartResponse {
    private UUID id;
    private BigDecimal totalAmount; // Tổng tiền tạm tính
    private int totalItems;         // Tổng số lượng hàng
    private List<CartItemDto> items;

    @Data
    @Builder
    public static class CartItemDto {
        private UUID id;            // ID của dòng trong giỏ hàng (để xóa/sửa)
        private UUID variantId;     // ID sản phẩm
        private String productName;
        private String skuCode;
        private String size;
        private String color;
        private String thumbnail;
        private BigDecimal unitPrice;
        private int quantity;
        private BigDecimal subTotal; // Thành tiền của món này (price * qty)
        private int maxStock;        // Trả về tồn kho hiện tại để Frontend giới hạn input
    }
}