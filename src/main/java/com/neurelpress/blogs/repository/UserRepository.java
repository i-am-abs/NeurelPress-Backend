package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(com.neurelpress.blogs.constants.enums.AuthProvider provider, String providerId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
