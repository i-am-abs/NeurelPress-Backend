package com.neurelpress.blogs.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "article_analytics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleAnalytics {

    @Id
    private UUID articleId;

    private Article article;

    @Builder.Default
    private long views24h = 0;

    @Builder.Default
    private long views7d = 0;

    @Builder.Default
    private long views30d = 0;

    @Indexed
    @Builder.Default
    private double trendingScore = 0.0;

    @LastModifiedDate
    private Instant lastCalculated;
}
