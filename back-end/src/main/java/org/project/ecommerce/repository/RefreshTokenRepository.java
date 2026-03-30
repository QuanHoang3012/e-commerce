package org.project.ecommerce.repository;

import org.project.ecommerce.entities.RefreshToken;
import org.project.ecommerce.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user = :user")
    void revokeAllByUser(User user);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.user = :user AND (r.revoked = true OR r.expiresAt < CURRENT_TIMESTAMP)")
    void deleteExpiredAndRevokedByUser(User user);
}
