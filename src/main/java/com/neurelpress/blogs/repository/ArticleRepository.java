package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.constants.ArticleStatus;
import com.neurelpress.blogs.dao.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArticleRepository extends MongoRepository<Article, UUID> {

    Optional<Article> findBySlug(String slug);

    Optional<Article> findBySlugAndStatus(String slug, ArticleStatus status);

    default Optional<Article> findPublishedBySlug(String slug) {
        return findBySlugAndStatus(slug, ArticleStatus.PUBLISHED);
    }

    Page<Article> findByStatusOrderByPublishedAtDesc(ArticleStatus status, Pageable pageable);

    @Query("{ 'author.$id': ?0, 'status': ?1 }")
    Page<Article> findByAuthorIdAndStatusOrderByCreatedAtDesc(UUID authorId, ArticleStatus status, Pageable pageable);

    @Query("{ 'author.$id': ?0 }")
    Page<Article> findByAuthorIdOrderByCreatedAtDesc(UUID authorId, Pageable pageable);

    @Query(value = "{ 'tags.slug': ?0, 'status': 'PUBLISHED' }", sort = "{ 'publishedAt': -1 }")
    Page<Article> findByTagSlug(String tagSlug, Pageable pageable);

    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'views': 1 } }")
    void incrementViews(UUID id);

    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'claps': 1 } }")
    void incrementClaps(UUID id);

    boolean existsBySlug(String slug);

    @Query(value = "{ 'author.$id': ?0, 'status': 'PUBLISHED' }", count = true)
    long countPublishedByAuthor(UUID authorId);
}
