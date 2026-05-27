package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookmarkRepository extends MongoRepository<Bookmark, UUID> {

    @Query("{ 'user.$id': ?0, 'article.$id': ?1 }")
    Optional<Bookmark> findByUserIdAndArticleId(UUID userId, UUID articleId);

    @Query(value = "{ 'user.$id': ?0, 'article.$id': ?1 }", exists = true)
    boolean existsByUserIdAndArticleId(UUID userId, UUID articleId);

    @Query(value = "{ 'user.$id': ?0 }", sort = "{ 'createdAt': -1 }")
    Page<Bookmark> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Query(value = "{ 'article.$id': ?0 }", count = true)
    long countByArticleId(UUID articleId);
}
