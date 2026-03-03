package com.neurelpress.blogs.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.FetchType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "article_analytics", indexes = {
        @Index(name = "idx_analytics_trending_score", columnList = "trendingScore")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleAnalytics {

    @Id
    private UUID articleId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private Article article;

    @Column(nullable = false)
    @Builder.Default
    private long views24h = 0;

    @Column(nullable = false)
    @Builder.Default
    private long views7d = 0;

    @Column(nullable = false)
    @Builder.Default
    private long views30d = 0;

    @Column(nullable = false)
    @Builder.Default
    private double trendingScore = 0.0;

    @UpdateTimestamp
    private Instant lastCalculated;
}
