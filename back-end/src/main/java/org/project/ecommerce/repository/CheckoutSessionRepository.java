package org.project.ecommerce.repository;

import java.util.Optional;
import java.util.UUID;

import org.project.ecommerce.entities.CheckoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckoutSessionRepository extends JpaRepository<CheckoutSession, UUID> {
    
    /**
     * Tìm checkout session theo session ID
     */
    Optional<CheckoutSession> findBySessionId(String sessionId);
    Optional<CheckoutSession> findBySessionIdAndIsCompleted(String sessionId, Boolean isCompleted);
}
