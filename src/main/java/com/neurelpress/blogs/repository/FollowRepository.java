package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends MongoRepository<Follow, UUID> {

    @Query("{ 'follower.$id': ?0, 'following.$id': ?1 }")
    Optional<Follow> findByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    @Query(value = "{ 'follower.$id': ?0, 'following.$id': ?1 }", exists = true)
    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    @Query("{ 'following.$id': ?0 }")
    Page<Follow> findByFollowingId(UUID followingId, Pageable pageable);

    @Query("{ 'follower.$id': ?0 }")
    Page<Follow> findByFollowerId(UUID followerId, Pageable pageable);

    @Query(value = "{ 'following.$id': ?0 }", count = true)
    long countByFollowingId(UUID followingId);

    @Query(value = "{ 'follower.$id': ?0 }", count = true)
    long countByFollowerId(UUID followerId);
}
