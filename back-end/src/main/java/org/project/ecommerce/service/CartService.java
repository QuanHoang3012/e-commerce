package org.project.ecommerce.service;

import org.project.ecommerce.dto.request.AddToCartRequest;
import org.project.ecommerce.dto.request.UpdateCartRequest;
import org.project.ecommerce.dto.response.CartResponse;
import org.project.ecommerce.entities.Cart;

import java.util.UUID;

public interface CartService {
    // Lấy giỏ hàng
    CartResponse getCart(UUID cartId);

    Cart createNewCart();
    // Thêm vào giỏ
    CartResponse addToCart(UUID cartId, AddToCartRequest request);

    // Cập nhật số lượng
    CartResponse updateCartItem(UUID cartId, UpdateCartRequest request);

    // Xóa món khỏi giỏ
    CartResponse removeCartItem(UUID cartId, UUID cartItemId);
}
