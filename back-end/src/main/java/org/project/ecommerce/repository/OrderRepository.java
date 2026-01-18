package org.project.ecommerce.repository;

import java.util.Optional;
import java.util.UUID;

import org.project.ecommerce.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    /**
     * Tìm đơn hàng theo tracking number
     * Dùng cho tracking công khai (không cần login)
     */
    Optional<Order> findByTrackingNumber(String trackingNumber);
}
