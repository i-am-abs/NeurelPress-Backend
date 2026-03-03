package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, UUID> {

    @Query("SELECT e FROM EmailOtp e WHERE e.user.email = :email AND e.code = :code AND e.used = false")
    Optional<EmailOtp> findActiveByEmailAndCode(@Param("email") String email, @Param("code") String code);

    @Modifying
    @Query("DELETE FROM EmailOtp e WHERE e.user.id = :userId OR e.expiresAt < :now")
    void deleteByUserIdOrExpired(@Param("userId") UUID userId, @Param("now") Instant now);
}
