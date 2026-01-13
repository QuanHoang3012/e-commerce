package org.project.ecommerce.mapper;

import lombok.Builder;
import org.project.ecommerce.dto.response.ProductDetailResponse;
import org.project.ecommerce.dto.response.ProductListResponse;
import org.project.ecommerce.entities.Product;
import org.project.ecommerce.entities.ProductImage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Component
public class ProductMapper {
    public ProductListResponse toListResponse(Product product) {
        String thumbnail = (product.getImages() != null && !product.getImages().isEmpty())
                ? product.getImages().get(0).getImageUrl()
                : null;

        return ProductListResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .minPrice(product.getMinPrice()) // Lấy giá cache
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .categorySlug(product.getCategory() != null ? product.getCategory().getSlug() : null)
                .thumbnail(thumbnail)
                .build();
    }

    // Map cho Detail: Nhận min/max từ bên ngoài (Service truyền vào)
    public ProductDetailResponse toDetailResponse(Product product, BigDecimal minPrice, BigDecimal maxPrice) {
        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .minPrice(minPrice) // Set giá min tính được
                .maxPrice(maxPrice) // Set giá max tính được
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .images(product.getImages().stream()
                        .map(ProductImage::getImageUrl)
                        .collect(Collectors.toList()))
                .variants(product.getVariants().stream()
                        .map(v -> ProductDetailResponse.VariantDto.builder()
                                .id(v.getId())
                                .skuCode(v.getSkuCode())
                                .size(v.getSize())
                                .color(v.getColor())
                                .price(v.getPrice()) // Map giá variant
                                .stockQuantity(v.getStockQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
