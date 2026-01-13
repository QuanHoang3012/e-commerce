package org.project.ecommerce.repository;

import org.project.ecommerce.dto.response.ProductPriceRange;
import org.project.ecommerce.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> , JpaSpecificationExecutor<Product> {
    @Query("SELECT min(v.price) as minPrice, max(v.price) as maxPrice " +
            "FROM ProductVariant v WHERE v.product.id = :productId")
    ProductPriceRange getPriceRangeByProductId(@Param("productId") UUID productId);
}
