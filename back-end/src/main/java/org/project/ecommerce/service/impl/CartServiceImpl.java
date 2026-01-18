package org.project.ecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import org.project.ecommerce.constant.CartStatus;
import org.project.ecommerce.dto.request.AddToCartRequest;
import org.project.ecommerce.dto.request.UpdateCartRequest;
import org.project.ecommerce.dto.response.CartResponse;
import org.project.ecommerce.entities.Cart;
import org.project.ecommerce.entities.CartItem;
import org.project.ecommerce.entities.ProductVariant;
import org.project.ecommerce.exception.CustomException;
import org.project.ecommerce.mapper.CartMapper;
import org.project.ecommerce.repository.CartItemRepository;
import org.project.ecommerce.repository.CartRepository;
import org.project.ecommerce.repository.ProductVariantRepository;
import org.project.ecommerce.service.CartService;
import org.project.ecommerce.service.InventoryReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartMapper cartMapper;
    private final InventoryReservationService reservationService;

    @Override
    public CartResponse getCart(UUID cartId) {
        if (cartId == null) {
            return CartResponse.builder().totalItems(0).totalAmount(BigDecimal.ZERO).build();
        }
        // Tìm theo ID, nếu không thấy (do DB bị reset chẳng hạn) thì return rỗng
        return cartRepository.findById(cartId)
                .map(cartMapper::toResponse)
                .orElse(CartResponse.builder().totalItems(0).totalAmount(BigDecimal.ZERO).build());
    }

    @Override
    @Transactional
    public CartResponse addToCart(UUID cartId, AddToCartRequest request) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CustomException("Giỏ hàng không tồn tại", HttpStatus.NOT_FOUND.value()));

        // 1. Kiểm tra sản phẩm tồn tại và TỒN KHO
        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new CustomException("Sản phẩm không tồn tại", HttpStatus.NOT_FOUND.value()));

        // Check tồn kho sơ bộ (Physical Stock)
        if (variant.getStockQuantity() < request.getQuantity()) {
            throw new CustomException("Kho chỉ còn " + variant.getStockQuantity() + " sản phẩm",
                    HttpStatus.BAD_REQUEST.value());
        }

        // 2. Kiểm tra xem món này đã có trong giỏ chưa
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getVariant().getId().equals(request.getVariantId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            // Nếu có rồi -> Cộng dồn số lượng
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + request.getQuantity();

            // Validate lại tồn kho với tổng số lượng mới
            if (variant.getStockQuantity() < newQuantity) {
                throw new CustomException(
                        "Tổng số lượng vượt quá tồn kho hiện tại (" + variant.getStockQuantity() + ")",
                        HttpStatus.BAD_REQUEST.value());
            }
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            // Nếu chưa có -> Tạo item mới
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem); // Sync list để return
            cartItemRepository.save(newItem);
        }

        // Update thời gian hoạt động của giỏ
        cartRepository.save(cart);

        return cartMapper.toResponse(cart);
    }

    @Override
    public Cart createNewCart() {
        Cart newCart = Cart.builder()
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();
        // ID (UUID) sẽ tự sinh khi save nhờ @GeneratedValue trong Entity
        return cartRepository.save(newCart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(UUID cartId,  UpdateCartRequest request) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CustomException("Giỏ hàng không tồn tại", HttpStatus.NOT_FOUND.value()));

        CartItem item = cartItemRepository.findById(request.getItemId())
                .orElseThrow(
                        () -> new CustomException("Món hàng không tồn tại trong giỏ", HttpStatus.NOT_FOUND.value()));

        // Security check: Đảm bảo item này thuộc về cart của token này
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new CustomException("Không có quyền sửa món hàng này", HttpStatus.FORBIDDEN.value());
        }

        // Validate số lượng > 0
        if (request.getQuantity() <= 0) {
            cartItemRepository.delete(item); // Nếu = 0 thì xóa luôn
        } else {
            // Check AVAILABLE STOCK (Physical - Reserved)
            int availableStock = reservationService.getAvailableStock(item.getVariant().getId());

            if (availableStock < request.getQuantity()) {
                throw new CustomException(
                        "Chỉ còn " + availableStock + " sản phẩm có thể đặt",
                        HttpStatus.BAD_REQUEST.value());
            }

            item.setQuantity(request.getQuantity());
            cartItemRepository.save(item);
        }

        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeCartItem(UUID cartId, UUID cartItemId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CustomException("Giỏ hàng không tồn tại", HttpStatus.NOT_FOUND.value()));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CustomException("Món hàng không tồn tại", HttpStatus.NOT_FOUND.value()));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new CustomException("Không có quyền xóa", HttpStatus.FORBIDDEN.value());
        }

        cart.getItems().remove(item); // Xóa khỏi list memory để mapper render đúng
        cartItemRepository.delete(item); // Xóa trong DB

        return cartMapper.toResponse(cart);
    }

}