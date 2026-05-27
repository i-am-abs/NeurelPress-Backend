package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

    @Query(value = "{ '$or': [ { 'user.$id': ?0 }, { 'expiresAt': { '$lt': ?1 } } ] }", delete = true)
    void deleteByUserIdOrExpired(UUID userId, Instant now);
}
