package org.project.ecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import org.project.ecommerce.dto.PageDTO;
import org.project.ecommerce.dto.response.ProductDetailResponse;
import org.project.ecommerce.dto.response.ProductListResponse;
import org.project.ecommerce.entities.Product;
import org.project.ecommerce.exception.CustomException;
import org.project.ecommerce.mapper.ProductMapper;
import org.project.ecommerce.repository.ProductRepository;
import org.project.ecommerce.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public PageDTO<ProductListResponse> getProducts(String categorySlug, BigDecimal minPrice, BigDecimal maxPrice,
            Pageable pageable) {

        // 1. Xây dựng bộ lọc động (Specification)
        Specification<Product> spec = (root, query, cb) -> cb.conjunction();

        // Lọc theo Category Slug (nếu có)
        if (categorySlug != null && !categorySlug.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("slug"), categorySlug));
        }

        // Lọc theo Khoảng giá (Min)
        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("basePrice"), minPrice));
        }

        // Lọc theo Khoảng giá (Max)
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("basePrice"), maxPrice));
        }

        // 2. Query Database với bộ lọc và phân trang
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        // 3. Convert Entity sang DTO và đóng gói vào PageDTO
        Page<ProductListResponse> dtoPage = productPage.map(productMapper::toListResponse);

        return PageDTO.from(dtoPage);
    }

    @Override
    public ProductDetailResponse getProductDetail(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CustomException("Sản phẩm không tồn tại", HttpStatus.NOT_FOUND.value()));

        return productMapper.toDetailResponse(product);
    }
}
