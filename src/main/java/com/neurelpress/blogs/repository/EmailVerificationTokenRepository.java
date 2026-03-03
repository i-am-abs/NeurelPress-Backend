package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByTokenAndUsedFalse(String token);

    @Modifying
    @Query("DELETE FROM EmailVerificationToken e WHERE e.user.id = :userId OR e.expiresAt < :now")
    void deleteByUserIdOrExpired(@Param("userId") UUID userId, @Param("now") Instant now);
}
