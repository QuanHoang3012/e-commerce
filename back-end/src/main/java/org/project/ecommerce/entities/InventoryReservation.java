package org.project.ecommerce.entities;

import jakarta.persistence.*;
import lombok.*;
import org.project.ecommerce.base.BaseEntity;
import org.project.ecommerce.constant.ReservationStatus;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "inventory_reservations", indexes = {
    @Index(name = "idx_cart_id_status", columnList = "cart_id, status"),
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
     * Cart ID từ cookie - dùng để tracking và quản lý reservation
     */
    @Column(name = "cart_id", nullable = false)
    private String cartId;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = ReservationStatus.ACTIVE;
        }
    }
}
