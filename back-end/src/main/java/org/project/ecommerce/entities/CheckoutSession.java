package org.project.ecommerce.entities;

import java.time.Instant;
import java.util.List;

import org.project.ecommerce.base.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
     * Liên kết với Cart - cart gốc khởi tạo session này
     * 1 Cart có thể có nhiều checkout sessions
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

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
    @Builder.Default
    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    /**
     * Các inventory reservations thuộc session này
     */
    @OneToMany(mappedBy = "checkoutSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryReservation> inventoryReservations;
}
