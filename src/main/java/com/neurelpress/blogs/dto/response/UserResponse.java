package com.neurelpress.blogs.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,

        String username,
        String email,
        String displayName,
        String headline,
        String bio,
        String avatarUrl,
        String role,
        String authProvider,
        String githubUrl,
        String linkedinUrl,
        String websiteUrl,
        String techTags,

        boolean verified,

        int followersCount,
        int followingCount,

        Long publishedArticleCount,
        List<String> allowedLoginMethods,

        Instant createdAt,

        Instant lastSignInAt
) {
}
