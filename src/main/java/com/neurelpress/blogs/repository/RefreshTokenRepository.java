package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    @Query("{ 'user.$id': ?0 }")
    @Update("{ '$set': { 'revoked': true } }")
    void revokeAllByUserId(UUID userId);
}
