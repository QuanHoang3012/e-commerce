package org.project.ecommerce.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProductDetailResponse {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private String categoryName;
    private List<String> images;
    private List<VariantDto> variants;

    @Data
    @Builder
    public static class VariantDto {
        private UUID id;
        private String skuCode;
        private String size;
        private String color;
        private Integer stockQuantity;
    }
}
