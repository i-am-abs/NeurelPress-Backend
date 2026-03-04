package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.ArticleClap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArticleClapRepository extends JpaRepository<ArticleClap, UUID> {

    Optional<ArticleClap> findByUserIdAndArticleId(UUID userId, UUID articleId);

    boolean existsByUserIdAndArticleId(UUID userId, UUID articleId);
}
