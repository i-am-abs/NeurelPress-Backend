package com.neurelpress.blogs.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    private String title;

    private String author;

    private String description;

    private String coverUrl;

    private String category;

    @Builder.Default
    private double rating = 0.0;

    @Builder.Default
    private int referencedCount = 0;

    private String affiliateUrl;

    @CreatedDate
    private Instant createdAt;
}
