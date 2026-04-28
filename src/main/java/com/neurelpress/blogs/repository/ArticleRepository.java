package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.constants.ArticleStatus;
import com.neurelpress.blogs.dao.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {

    Optional<Article> findBySlug(String slug);

    @Query("SELECT a FROM Article a JOIN FETCH a.author WHERE a.slug = :slug AND a.status = 'PUBLISHED'")
    Optional<Article> findPublishedBySlug(@Param("slug") String slug);

    Page<Article> findByStatusOrderByPublishedAtDesc(ArticleStatus status, Pageable pageable);

    Page<Article> findByAuthorIdAndStatusOrderByCreatedAtDesc(UUID authorId, ArticleStatus status, Pageable pageable);

    Page<Article> findByAuthorIdOrderByCreatedAtDesc(UUID authorId, Pageable pageable);

    @Query("SELECT a FROM Article a JOIN a.tags t WHERE t.slug = :tagSlug AND a.status = 'PUBLISHED' ORDER BY a.publishedAt DESC")
    Page<Article> findByTagSlug(@Param("tagSlug") String tagSlug, Pageable pageable);

    @Modifying
    @Query("UPDATE Article a SET a.views = a.views + 1 WHERE a.id = :id")
    void incrementViews(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Article a SET a.claps = a.claps + 1 WHERE a.id = :id")
    void incrementClaps(@Param("id") UUID id);

    boolean existsBySlug(String slug);

    @Query("SELECT COUNT(a) FROM Article a WHERE a.author.id = :authorId AND a.status = 'PUBLISHED'")
    long countPublishedByAuthor(@Param("authorId") UUID authorId);
}
