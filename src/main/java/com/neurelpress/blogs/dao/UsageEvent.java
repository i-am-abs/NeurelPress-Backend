package com.neurelpress.blogs.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "usage_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageEvent {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Indexed
    private String eventName;

    @Indexed
    private UUID userId;

    private String entityType;

    private String entityId;

    private String sessionId;

    private String path;

    private String referrer;

    private String country;

    private String device;

    private String metadata;

    @CreatedDate
    @Indexed
    private Instant createdAt;
}
