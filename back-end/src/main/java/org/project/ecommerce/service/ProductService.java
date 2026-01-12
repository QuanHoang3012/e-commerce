package org.project.ecommerce.service;

import org.project.ecommerce.dto.PageDTO;
import org.project.ecommerce.dto.response.ProductDetailResponse;
import org.project.ecommerce.dto.response.ProductListResponse;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.UUID;

public interface ProductService {
    PageDTO<ProductListResponse> getProducts(String categorySlug, BigDecimal minPrice, BigDecimal maxPrice,
            Pageable pageable);

    ProductDetailResponse getProductDetail(UUID id);
}
