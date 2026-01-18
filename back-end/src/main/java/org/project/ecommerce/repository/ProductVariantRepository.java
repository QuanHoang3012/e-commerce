package org.project.ecommerce.repository;

import jakarta.persistence.LockModeType;
import org.project.ecommerce.entities.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    /**
     * Tìm variant với PESSIMISTIC_WRITE lock
     * Dùng khi cần update stock_quantity để tránh race condition
     * Các transaction khác phải chờ lock được release
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ProductVariant v WHERE v.id = :id")
    Optional<ProductVariant> findByIdWithLock(@Param("id") UUID id);
}

