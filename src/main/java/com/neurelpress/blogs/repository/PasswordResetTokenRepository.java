package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.user.id = :userId OR p.expiresAt < :now")
    void deleteByUserIdOrExpired(@Param("userId") UUID userId, @Param("now") Instant now);
}
