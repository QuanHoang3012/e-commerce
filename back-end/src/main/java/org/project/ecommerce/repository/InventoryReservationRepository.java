package org.project.ecommerce.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.project.ecommerce.constant.ReservationStatus;
import org.project.ecommerce.entities.CheckoutSession;
import org.project.ecommerce.entities.InventoryReservation;
import org.project.ecommerce.entities.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, UUID> {

    /**
     * Tìm reservation đang ACTIVE của một checkout session cụ thể
     */
    List<InventoryReservation> findByCheckoutSessionAndStatus(CheckoutSession session, ReservationStatus status);

    /**
     * Tìm reservation của một variant và checkout session cụ thể
     */
    Optional<InventoryReservation> findByVariantAndCheckoutSessionAndStatus(
            ProductVariant variant, CheckoutSession session, ReservationStatus status);

    /**
     * Tính tổng số lượng đã được reserve cho một variant (chỉ ACTIVE)
     */
    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM InventoryReservation r " +
            "WHERE r.variant.id = :variantId AND r.status = 'ACTIVE'")
    Integer sumReservedQuantityByVariantId(@Param("variantId") UUID variantId);

    /**
     * Tìm tất cả reservation đã HẾT HẠN (quá 15 phút) và còn ACTIVE
     * Dùng cho scheduled task tự động release
     */
    @Query("SELECT r FROM InventoryReservation r " +
            "WHERE r.status = 'ACTIVE' " +
            "AND r.createdAt < :expiryTime")
    List<InventoryReservation> findExpiredReservations(@Param("expiryTime") Instant expiryTime);

    /**
     * Bulk update status của các reservations
     */
    @Modifying
    @Query("UPDATE InventoryReservation r SET r.status = :newStatus " +
            "WHERE r.id IN :ids")
    void updateStatusByIds(@Param("ids") List<UUID> ids, @Param("newStatus") ReservationStatus newStatus);

    /**
     * Bulk update status của các reservation đã hết hạn thành EXPIRED
     * Trả về số lượng records đã update
     */
    @Modifying
    @Query("UPDATE InventoryReservation r SET r.status = 'EXPIRED' " +
            "WHERE r.status = 'ACTIVE' AND r.createdAt < :expiryTime")
    int expireReservationsByTime(@Param("expiryTime") Instant expiryTime);

    /**
     * Xóa các reservation đã EXPIRED hoặc COMPLETED (cleanup định kỳ)
     */
    @Modifying
    @Query("DELETE FROM InventoryReservation r " +
            "WHERE r.status IN ('EXPIRED', 'COMPLETED') " +
            "AND r.updatedAt < :cleanupTime")
    void cleanupOldReservations(@Param("cleanupTime") Instant cleanupTime);
}
