package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.ArticleAnalytics;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArticleAnalyticsRepository extends MongoRepository<ArticleAnalytics, UUID> {

    @Query(value = "{ 'article.status': 'PUBLISHED' }", sort = "{ 'trendingScore': -1 }")
    List<ArticleAnalytics> findTrending(Pageable pageable);
}
