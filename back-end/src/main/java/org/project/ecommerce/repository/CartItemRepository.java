package org.project.ecommerce.repository;

import org.project.ecommerce.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    // Các hàm xóa, tìm kiếm item sẽ dùng mặc định của JpaRepository
}
