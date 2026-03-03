package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {

    Optional<Follow> findByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    Page<Follow> findByFollowingId(UUID followingId, Pageable pageable);

    Page<Follow> findByFollowerId(UUID followerId, Pageable pageable);

    long countByFollowingId(UUID followingId);

    long countByFollowerId(UUID followerId);
}
