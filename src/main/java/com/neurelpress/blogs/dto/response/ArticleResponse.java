package com.neurelpress.blogs.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ArticleResponse(
        UUID id,
        String title,
        String slug,
        String summary,
        String content,
        String coverImage,
        String status,

        int readTime,
        int claps,
        int bookmarksCount,
        int commentsCount,

        long views,

        String seoTitle,
        String seoDescription,
        String canonicalUrl,

        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt,

        AuthorSummary author,
        List<TagResponse> tags,
        List<BookResponse> books,

        Boolean bookmarked
) {
    public record AuthorSummary(
            UUID id,

            String username,
            String displayName,
            String avatarUrl,
            String bio
    ) {
    }
}
