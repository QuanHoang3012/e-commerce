package org.project.ecommerce.mapper;

import lombok.Builder;
import org.project.ecommerce.dto.response.ProductDetailResponse;
import org.project.ecommerce.dto.response.ProductListResponse;
import org.project.ecommerce.entities.Product;
import org.project.ecommerce.entities.ProductImage;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ProductMapper {
    public ProductListResponse toListResponse(Product product) {
        // Lấy ảnh đầu tiên làm thumbnail, nếu không có thì null
        String thumbnail = (product.getImages() != null && !product.getImages().isEmpty())
                ? product.getImages().get(0).getImageUrl()
                : null;

        return ProductListResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .basePrice(product.getBasePrice())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .categorySlug(product.getCategory() != null ? product.getCategory().getSlug() : null)
                .thumbnail(thumbnail)
                .build();
    }

    // Map cho trang chi tiết (Detail)
    public ProductDetailResponse toDetailResponse(Product product) {
        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                // Map list images
                .images(product.getImages().stream()
                        .map(ProductImage::getImageUrl)
                        .collect(Collectors.toList()))
                // Map list variants
                .variants(product.getVariants().stream()
                        .map(v -> ProductDetailResponse.VariantDto.builder()
                                .id(v.getId())
                                .skuCode(v.getSkuCode())
                                .size(v.getSize())
                                .color(v.getColor())
                                .stockQuantity(v.getStockQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
