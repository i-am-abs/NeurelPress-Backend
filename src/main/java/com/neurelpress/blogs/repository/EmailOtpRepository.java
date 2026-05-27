package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.EmailOtp;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailOtpRepository extends MongoRepository<EmailOtp, UUID> {

    @Query("{ 'email': ?0, 'code': ?1, 'used': false }")
    Optional<EmailOtp> findActiveByEmailAndCode(String email, String code);

    @Query(value = "{ '$or': [ { 'user.$id': ?0 }, { 'expiresAt': { '$lt': ?1 } } ] }", delete = true)
    void deleteByUserIdOrExpired(UUID userId, Instant now);
}
