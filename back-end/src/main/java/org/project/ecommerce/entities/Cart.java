package org.project.ecommerce.entities;


import jakarta.persistence.*;
import lombok.*;
import org.project.ecommerce.base.BaseEntity;
import org.project.ecommerce.constant.CartStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
}
