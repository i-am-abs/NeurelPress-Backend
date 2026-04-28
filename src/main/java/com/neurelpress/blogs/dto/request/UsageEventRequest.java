package com.neurelpress.blogs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record UsageEventRequest(

        @NotBlank(message = "eventName is required")
        @Size(max = 64)
        String eventName,

        @Size(max = 128)
        String entityType,

        @Size(max = 256)
        String entityId,

        @Size(max = 64)
        String sessionId,

        @Size(max = 256)
        String path,

        @Size(max = 64)
        String referrer,

        @Size(max = 64)
        String device,

        @Size(max = 1024)
        String metadata
) {
}
