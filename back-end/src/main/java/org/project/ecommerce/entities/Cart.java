package org.project.ecommerce.entities;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.project.ecommerce.base.BaseEntity;
import org.project.ecommerce.constant.CartStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cart")
@Builder
@Getter
@Setter
public class Cart extends BaseEntity {

    // Nếu khách login thì update vào đây
    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    private CartStatus status;

    @Column(name = "last_active_at")
    private Instant lastActiveAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items;

    /**
     * Các checkout sessions được tạo từ cart này
     * 1 Cart có thể có nhiều checkout sessions (user có thể checkout nhiều lần)
     */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CheckoutSession> checkoutSessions;

}
