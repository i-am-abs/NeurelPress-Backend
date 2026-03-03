package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {

    Optional<Bookmark> findByUserIdAndArticleId(UUID userId, UUID articleId);

    boolean existsByUserIdAndArticleId(UUID userId, UUID articleId);

    Page<Bookmark> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    long countByArticleId(UUID articleId);
}
