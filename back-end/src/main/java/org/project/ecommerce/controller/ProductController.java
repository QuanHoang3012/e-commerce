package org.project.ecommerce.controller;

import java.math.BigDecimal;
import java.util.UUID;

import org.project.ecommerce.base.BaseController;
import org.project.ecommerce.base.BaseResponse;
import org.project.ecommerce.dto.PageDTO;
import org.project.ecommerce.dto.response.ProductDetailResponse;
import org.project.ecommerce.dto.response.ProductListResponse;
import org.project.ecommerce.service.ProductService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "API quản lý sản phẩm - Danh sách và chi tiết sản phẩm")
public class ProductController extends BaseController {

    private final ProductService productService;

    /**
     * API xem danh sách sản phẩm (Catalog)
     * - Hỗ trợ lọc theo category, khoảng giá
     * - Hỗ trợ phân trang, sắp xếp
     * URL ví dụ: /api/v1/products?page=1&size=10&category=hoodie&minPrice=200000&sort=basePrice,asc
     */
    @GetMapping
    public BaseResponse<PageDTO<ProductListResponse>> getProducts(
            @RequestParam(defaultValue = "1") int page, // Frontend thường gửi page 1, nhưng Spring tính từ 0
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String category, // slug
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort // Mặc định mới nhất trước
    ) {
        // Xử lý page từ 1-based (Frontend) về 0-based (Backend)
        int pageNo = (page < 1) ? 0 : page - 1;

        // Tạo Pageable từ request
        // Cấu trúc sort ví dụ: ?sort=basePrice,asc
        Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);

        Pageable pageable = PageRequest.of(pageNo, size, sorting);

        PageDTO<ProductListResponse> data = productService.getProducts(category, minPrice, maxPrice, pageable);

        return wrapSuccess(data);
    }

    /**
     * API xem chi tiết sản phẩm (Detail)
     * - Load cả SKU, Size, Màu
     */
    @GetMapping("/{id}")
    public BaseResponse<ProductDetailResponse> getProductDetail(@PathVariable UUID id) {
        ProductDetailResponse data = productService.getProductDetail(id);
        return wrapSuccess(data);
    }
}
