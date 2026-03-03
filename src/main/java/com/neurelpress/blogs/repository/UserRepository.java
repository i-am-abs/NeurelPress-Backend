package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(com.neurelpress.blogs.constants.enums.AuthProvider provider, String providerId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("""
            SELECT u FROM User u
            WHERE lower(u.username) LIKE lower(concat('%', :query, '%'))
               OR lower(u.displayName) LIKE lower(concat('%', :query, '%'))
            ORDER BY u.createdAt DESC
            """)
    Page<User> search(@Param("query") String query, Pageable pageable);
}
