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
@Table(name = "usage_events", indexes = {
        @Index(name = "idx_usage_event_name", columnList = "eventName"),
        @Index(name = "idx_usage_event_user", columnList = "userId"),
        @Index(name = "idx_usage_event_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 64)
    private String eventName;

    private UUID userId;

    @Column(length = 128)
    private String entityType;

    private String entityId;

    @Column(length = 64)
    private String sessionId;

    @Column(length = 256)
    private String path;

    @Column(length = 64)
    private String referrer;

    @Column(length = 32)
    private String country;

    @Column(length = 64)
    private String device;

    @Column(length = 1024)
    private String metadata;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
