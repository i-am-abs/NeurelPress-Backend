package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.ArticleClap;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArticleClapRepository extends MongoRepository<ArticleClap, UUID> {

    @Query("{ 'user.$id': ?0, 'article.$id': ?1 }")
    Optional<ArticleClap> findByUserIdAndArticleId(UUID userId, UUID articleId);

    @Query(value = "{ 'user.$id': ?0, 'article.$id': ?1 }", exists = true)
    boolean existsByUserIdAndArticleId(UUID userId, UUID articleId);
}
