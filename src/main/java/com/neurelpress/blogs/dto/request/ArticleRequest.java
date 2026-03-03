package com.neurelpress.blogs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ArticleRequest(
        @NotBlank @Size(max = 255)
        String title,

        @Size(max = 500)
        String summary,

        @NotBlank
        String content,

        String coverImage,
        List<String> tagSlugs,
        List<UUID> bookIds,

        @Size(max = 255)
        String seoTitle,

        @Size(max = 300)
        String seoDescription,

        String canonicalUrl
) {}
