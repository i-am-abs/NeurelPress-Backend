package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    Optional<Tag> findBySlug(String slug);

    Optional<Tag> findByNameIgnoreCase(String name);

    @Query("SELECT t FROM Tag t ORDER BY t.articleCount DESC")
    List<Tag> findTopTags(org.springframework.data.domain.Pageable pageable);

    List<Tag> findBySlugIn(List<String> slugs);
}
