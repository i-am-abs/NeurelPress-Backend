package com.neurelpress.blogs.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "crash_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrashReport {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Indexed
    private String source;

    private String message;

    private String exceptionType;

    private String stackTrace;

    private String requestPath;

    private String requestMethod;

    private String httpStatus;

    private UUID userId;

    private String sessionId;

    private String release;

    private String userAgent;

    @CreatedDate
    @Indexed
    private Instant createdAt;
}
