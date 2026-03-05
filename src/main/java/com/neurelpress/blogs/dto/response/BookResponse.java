package com.neurelpress.blogs.dto.response;

import java.io.Serializable;
import java.util.UUID;

public record BookResponse(
        UUID id,

        String title,
        String author,
        String description,
        String coverUrl,
        String category,

        double affiliateUrl,
        double rating,

        String referencedCount
) implements Serializable {
}
