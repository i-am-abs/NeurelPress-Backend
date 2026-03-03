package com.neurelpress.blogs.dto.response;

import java.util.UUID;

public record TagResponse(
        UUID id,

        String name,
        String slug,
        String description,

        int articleCount
) {}
