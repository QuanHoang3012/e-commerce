package org.project.ecommerce.entities;

import jakarta.persistence.*;
import lombok.*;
import org.project.ecommerce.base.BaseEntity;

import java.math.BigDecimal;


@Entity
@Table(name = "order_items")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class OrderItem extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    private Integer quantity;

    // Lưu giá tại thời điểm mua (Tránh việc sau này giá sản phẩm gốc thay đổi)
    @Column(name = "price_at_purchase")
    private BigDecimal priceAtPurchase;
    
    // Tổng tiền (quantity * priceAtPurchase)
    @Column(name = "subtotal")
    private BigDecimal subtotal;
}
