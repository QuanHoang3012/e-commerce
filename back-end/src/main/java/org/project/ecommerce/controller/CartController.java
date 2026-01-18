package org.project.ecommerce.controller;

import java.time.Duration;
import java.util.UUID;

import org.project.ecommerce.base.BaseController;
import org.project.ecommerce.base.BaseResponse;
import org.project.ecommerce.dto.request.AddToCartRequest;
import org.project.ecommerce.dto.request.UpdateCartRequest;
import org.project.ecommerce.dto.response.CartResponse;
import org.project.ecommerce.entities.Cart;
import org.project.ecommerce.service.CartService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "API quản lý giỏ hàng - Thêm, cập nhật, xóa sản phẩm")
public class CartController extends BaseController {

    private final CartService cartService;
    private static final String CART_COOKIE_NAME = "CART_ID";
    // Helper để lấy token từ header, nếu không có thì báo lỗi
    private void setCartCookie(HttpServletResponse response, String cartId) {
        ResponseCookie cookie = ResponseCookie.from(CART_COOKIE_NAME, cartId)
                .httpOnly(true)  // Quan trọng: JS không đọc được (Bảo mật)
                .secure(false)   // Để false nếu chạy localhost (http), lên prod (https) thì để true
                .path("/")       // Có hiệu lực toàn site
                .maxAge(Duration.ofDays(7)) // Lưu 7 ngày
                .sameSite("Lax") // Chống CSRF cơ bản
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // 1. Xem giỏ hàng
    @GetMapping
    public BaseResponse<CartResponse> getCart(
            @CookieValue(name = CART_COOKIE_NAME, required = false) UUID cartId,
            HttpServletResponse response
    ) {
        if (cartId == null) {
            // Chưa có Cookie -> Tạo Cart mới -> Gắn Cookie vào Response
            Cart newCart = cartService.createNewCart();
            setCartCookie(response, newCart.getId().toString());
            return wrapSuccess(cartService.getCart(newCart.getId()));
        }

        // Đã có Cookie -> Lấy Cart cũ
        // Case phụ: Cookie còn nhưng DB mất Cart (do dọn dẹp) -> Tạo lại
        try {
            return wrapSuccess(cartService.getCart(cartId));
        } catch (Exception e) {
            // Fallback nếu cartId không tìm thấy
            Cart newCart = cartService.createNewCart();
            setCartCookie(response, newCart.getId().toString());
            return wrapSuccess(cartService.getCart(newCart.getId()));
        }
    }

    // 2. Thêm vào giỏ
    @PostMapping("/items")
    public BaseResponse<CartResponse> addToCart(
            @CookieValue(name = CART_COOKIE_NAME, required = false) UUID cartId,
            @RequestBody AddToCartRequest request,
            HttpServletResponse response
    ) {
        UUID finalCartId = cartId;

        // Trường hợp khách vào web lần đầu mà bấm "Mua ngay" luôn (chưa có cookie)
        if (finalCartId == null) {
            Cart newCart = cartService.createNewCart();
            finalCartId = newCart.getId();
            setCartCookie(response, finalCartId.toString());
        }

        return wrapSuccess(cartService.addToCart(finalCartId, request));
    }

    // 3. Cập nhật & Xóa (Bắt buộc phải có Cookie rồi mới gọi được mấy cái này)
    @PutMapping("/items/{itemId}")
    public BaseResponse<CartResponse> updateCartItem(
            @CookieValue(name = CART_COOKIE_NAME) UUID cartId,
            @RequestBody UpdateCartRequest request
    ) {
        return wrapSuccess(cartService.updateCartItem(cartId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public BaseResponse<CartResponse> removeCartItem(
            @CookieValue(name = CART_COOKIE_NAME) UUID cartId,
            @PathVariable UUID itemId
    ) {
        return wrapSuccess(cartService.removeCartItem(cartId, itemId));
    }
}