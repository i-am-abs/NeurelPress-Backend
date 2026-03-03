package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.ArticleAnalytics;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArticleAnalyticsRepository extends JpaRepository<ArticleAnalytics, UUID> {

    @Query("SELECT aa FROM ArticleAnalytics aa JOIN FETCH aa.article a JOIN FETCH a.author WHERE a.status = 'PUBLISHED' ORDER BY aa.trendingScore DESC")
    List<ArticleAnalytics> findTrending(Pageable pageable);
}
