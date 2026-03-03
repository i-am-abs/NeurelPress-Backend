package com.neurelpress.blogs.dto.response;

import java.util.UUID;

public record QuoteResponse(
        UUID id,

        String text,
        String author,
        String source
) {}
