package org.project.ecommerce.entities;

import jakarta.persistence.*;
import lombok.*;
import org.project.ecommerce.base.BaseEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * Checkout Session - Track mỗi lần checkout
 * 1 Cart có thể có nhiều checkout sessions (nhiều lần initiate)
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "checkout_sessions", indexes = {
    @Index(name = "idx_session_id", columnList = "session_id"),
    @Index(name = "idx_cart_id", columnList = "cart_id")
})
@Builder
@Getter
@Setter
public class CheckoutSession extends BaseEntity {

    /**
     * Session ID unique - dùng để tracking checkout
     */
    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId;

    /**
     * Cart ID - cart gốc khởi tạo session này
     */
    @Column(name = "cart_id", nullable = false)
    private UUID cartId;

    /**
     * Order đã tạo từ session này (snapshot data)
     */
    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    /**
     * Thời gian hết hạn session
     */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /**
     * Session đã hoàn tất chưa
     */
    @Column(name = "is_completed")
    private Boolean isCompleted = false;
}
