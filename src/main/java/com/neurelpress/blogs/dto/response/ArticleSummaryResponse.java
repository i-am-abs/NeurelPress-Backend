package com.neurelpress.blogs.dto.response;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ArticleSummaryResponse(
        UUID id,

        String title,
        String slug,
        String summary,
        String coverImage,
        String status,

        int readTime,
        int claps,
        int bookmarksCount,

        long views,

        Instant publishedAt,
        Instant createdAt,

        ArticleResponse.AuthorSummary author,
        List<TagResponse> tags
) implements Serializable {
}
