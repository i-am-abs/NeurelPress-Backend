package com.neurelpress.blogs.dao;

import com.neurelpress.blogs.constants.ArticleStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Document(collection = "articles")
@CompoundIndex(name = "idx_article_author_status", def = "{'author.$id': 1, 'status': 1}")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @DBRef
    private User author;

    private String title;

    @Indexed(unique = true)
    private String slug;

    private String summary;

    private String content;

    private String coverImage;

    @Indexed
    @Builder.Default
    private ArticleStatus status = ArticleStatus.DRAFT;

    @Builder.Default
    private int readTime = 0;

    @Indexed
    @Builder.Default
    private long views = 0;

    @Builder.Default
    private int claps = 0;

    @Builder.Default
    private int bookmarksCount = 0;

    @Builder.Default
    private int commentsCount = 0;

    private String seoTitle;

    private String seoDescription;

    private String canonicalUrl;

    @Indexed
    private Instant publishedAt;

    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @Builder.Default
    private Set<Book> books = new HashSet<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
