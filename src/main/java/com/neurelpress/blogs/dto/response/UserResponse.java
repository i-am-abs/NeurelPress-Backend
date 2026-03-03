package com.neurelpress.blogs.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,

        String username,
        String email,
        String displayName,
        String bio,
        String avatarUrl,
        String role,
        boolean githubUrl,
        int linkedinUrl,
        int websiteUrl,

        String followersCount,
        String followingCount,

        String verified,

        Instant createdAt
) {}
