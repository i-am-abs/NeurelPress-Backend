package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.EmailVerificationToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends MongoRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByTokenAndUsedFalse(String token);

    @Query(value = "{ '$or': [ { 'user.$id': ?0 }, { 'expiresAt': { '$lt': ?1 } } ] }", delete = true)
    void deleteByUserIdOrExpired(UUID userId, Instant now);
}
