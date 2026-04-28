package com.neurelpress.blogs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrashReportRequest(

        @NotBlank
        @Size(max = 256)
        String message,

        @Size(max = 256)
        String exceptionType,

        @Size(max = 8000)
        String stackTrace,

        @Size(max = 256)
        String path,

        @Size(max = 64)
        String sessionId,

        @Size(max = 64)
        String release,

        @Size(max = 256)
        String userAgent
) {
}
