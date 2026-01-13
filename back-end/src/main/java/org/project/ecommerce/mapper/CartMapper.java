package org.project.ecommerce.mapper;

import org.project.ecommerce.dto.response.CartResponse;
import org.project.ecommerce.entities.Cart;
import org.project.ecommerce.entities.CartItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

        public CartResponse toResponse(Cart cart) {
                if (cart == null) {
                        return null;
                }

                List<CartResponse.CartItemDto> itemDtos = cart.getItems() == null ? Collections.emptyList()
                                : cart.getItems().stream().map(this::toItemDto).collect(Collectors.toList());

                // Tính tổng tiền
                BigDecimal totalAmount = itemDtos.stream()
                                .map(CartResponse.CartItemDto::getSubTotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Tính tổng số lượng
                int totalItems = itemDtos.stream()
                                .mapToInt(CartResponse.CartItemDto::getQuantity)
                                .sum();

                return CartResponse.builder()
                                .id(cart.getId())
                                .totalAmount(totalAmount)
                                .totalItems(totalItems)
                                .items(itemDtos)
                                .build();
        }

        private CartResponse.CartItemDto toItemDto(CartItem item) {
                var variant = item.getVariant();
                var product = variant.getProduct();

                // Lấy ảnh thumbnail
                String thumb = (product.getImages() != null && !product.getImages().isEmpty())
                                ? product.getImages().get(0).getImageUrl()
                                : "";

                BigDecimal unitPrice = variant.getPrice();
                BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                return CartResponse.CartItemDto.builder()
                                .id(item.getId())
                                .variantId(variant.getId())
                                .productName(product.getName())
                                .skuCode(variant.getSkuCode())
                                .size(variant.getSize())
                                .color(variant.getColor())
                                .thumbnail(thumb)
                                .unitPrice(unitPrice)
                                .quantity(item.getQuantity())
                                .subTotal(subTotal)
                                .maxStock(variant.getStockQuantity()) // Trả về tồn kho để FE chặn
                                .build();
        }
}
