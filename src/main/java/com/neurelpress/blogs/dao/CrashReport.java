package com.neurelpress.blogs.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "crash_reports", indexes = {
        @Index(name = "idx_crash_source", columnList = "source"),
        @Index(name = "idx_crash_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrashReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 16)
    private String source;

    @Column(nullable = false, length = 256)
    private String message;

    @Column(length = 256)
    private String exceptionType;

    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    @Column(length = 256)
    private String requestPath;

    @Column(length = 8)
    private String requestMethod;

    @Column(length = 32)
    private String httpStatus;

    private UUID userId;

    @Column(length = 64)
    private String sessionId;

    @Column(length = 64)
    private String release;

    @Column(length = 256)
    private String userAgent;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
