package com.neurelpress.blogs.dao;

import com.neurelpress.blogs.constants.enums.ArticleStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "articles",
        indexes = {@Index(name = "idx_article_slug",
                columnList = "slug", unique = true),
                @Index(name = "idx_article_published_at",
                        columnList = "publishedAt"),
                @Index(name = "idx_article_views",
                        columnList = "views"),
                @Index(name = "idx_article_status",
                        columnList = "status"),
                @Index(name = "idx_article_author_status",
                        columnList = "author_id, status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private String coverImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ArticleStatus status = ArticleStatus.DRAFT;

    @Column(nullable = false)
    @Builder.Default
    private int readTime = 0;

    @Column(nullable = false)
    @Builder.Default
    private long views = 0;

    @Column(nullable = false)
    @Builder.Default
    private int claps = 0;

    @Column(nullable = false)
    @Builder.Default
    private int bookmarksCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private int commentsCount = 0;

    private String seoTitle;

    private String seoDescription;

    private String canonicalUrl;

    private Instant publishedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "article_tags", joinColumns = @JoinColumn(name = "article_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "article_books", joinColumns = @JoinColumn(name = "article_id"), inverseJoinColumns = @JoinColumn(name = "book_id"))
    @Builder.Default
    private Set<Book> books = new HashSet<>();

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
