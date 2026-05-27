package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.constants.AuthProvider;
import com.neurelpress.blogs.dao.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends MongoRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query(value = "{ '$or': [ { 'username': { '$regex': ?0, '$options': 'i' } }, { 'displayName': { '$regex': ?0, '$options': 'i' } } ] }",
            sort = "{ 'createdAt': -1 }")
    Page<User> search(String query, Pageable pageable);
}
