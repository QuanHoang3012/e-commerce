package org.project.ecommerce.entities;

import org.project.ecommerce.base.BaseEntity;
import org.project.ecommerce.constant.ReservationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "inventory_reservations", indexes = {
    @Index(name = "idx_session_status", columnList = "session_id, status"),
    @Index(name = "idx_variant_status", columnList = "variant_id, status")
})
@Builder
@Getter
@Setter
public class InventoryReservation extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(nullable = false)
    private Integer quantity;

    /**
     * Liên kết với CheckoutSession - mỗi reservation thuộc về một checkout session
     * Sử dụng sessionId (String) làm FK thay vì ID (UUID)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", referencedColumnName = "session_id", nullable = false)
    private CheckoutSession checkoutSession;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = ReservationStatus.ACTIVE;
        }
    }
}
