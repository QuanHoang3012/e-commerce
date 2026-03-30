package org.project.ecommerce.service.impl;

import java.math.BigDecimal;
import java.util.UUID;

import org.project.ecommerce.dto.PageDTO;
import org.project.ecommerce.dto.response.ProductDetailResponse;
import org.project.ecommerce.dto.response.ProductListResponse;
import org.project.ecommerce.dto.response.ProductPriceRange;
import org.project.ecommerce.entities.Product;
import org.project.ecommerce.exception.CustomException;
import org.project.ecommerce.mapper.ProductMapper;
import org.project.ecommerce.repository.ProductRepository;
import org.project.ecommerce.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public PageDTO<ProductListResponse> getProducts(String categorySlug, BigDecimal minPrice, BigDecimal maxPrice,
            Pageable pageable) {

        // 1. Xây dựng bộ lọc động (Specification)
        // Specification.where(null) là khởi tạo chuẩn, giúp code gọn hơn
        // cb.conjunction()
        Specification<Product> spec = (root, query, cb) -> cb.conjunction();

        // Lọc theo Category Slug (nếu có)
        if (categorySlug != null && !categorySlug.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("slug"), categorySlug));
        }

        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("minPrice"), minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("minPrice"), maxPrice));
        }

        // 2. Query Database
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        // 3. Convert Entity sang DTO
        Page<ProductListResponse> dtoPage = productPage.map(productMapper::toListResponse);

        return PageDTO.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetail(UUID id) {
        // 1. Fetch Product (Thông tin chung)
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CustomException("Sản phẩm không tồn tại", HttpStatus.NOT_FOUND.value()));

        // 2. Query Min/Max Price trực tiếp từ DB (Tối ưu hiệu năng)
        ProductPriceRange priceRange = productRepository.getPriceRangeByProductId(id);

        BigDecimal minPrice;
        BigDecimal maxPrice;

        // Xử lý trường hợp sản phẩm chưa có biến thể nào (Query trả về null)
        if (priceRange != null && priceRange.getMinPrice() != null) {
            minPrice = priceRange.getMinPrice();
            maxPrice = priceRange.getMaxPrice();
        } else {
            minPrice = product.getMinPrice();
            maxPrice = product.getMinPrice();
        }

        return productMapper.toDetailResponse(product, minPrice, maxPrice);
    }
}
