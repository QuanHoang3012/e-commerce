package org.project.ecommerce.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.project.ecommerce.constant.OrderStatus;
import org.project.ecommerce.entities.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    /**
     * Tìm đơn hàng theo tracking number
     * Dùng cho tracking công khai (không cần login)
     */
    Optional<Order> findByTrackingNumber(String trackingNumber);

    /**
     * Lấy danh sách đơn hàng theo status với phân trang
     * Dùng cho warehouse staff filter đơn hàng
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    
    /**
     * Tìm các order theo status và đã tạo trước thời điểm nào đó
     * Dùng cho cleanup expired PENDING orders
     */
    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, Instant createdAt);
}
