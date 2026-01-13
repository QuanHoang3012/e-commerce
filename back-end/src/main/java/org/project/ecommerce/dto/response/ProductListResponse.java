package org.project.ecommerce.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ProductListResponse {
    private UUID id;
    private String name;
    private BigDecimal minPrice;
    private String categoryName;
    private String categorySlug;
    private String thumbnail;
}