package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.Tag;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends MongoRepository<Tag, UUID> {

    Optional<Tag> findBySlug(String slug);

    Optional<Tag> findByNameIgnoreCase(String name);

    @Query(value = "{}", sort = "{ 'articleCount': -1 }")
    List<Tag> findTopTags(org.springframework.data.domain.Pageable pageable);

    List<Tag> findBySlugIn(List<String> slugs);
}
