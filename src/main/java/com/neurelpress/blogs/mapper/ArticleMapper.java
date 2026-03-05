package com.neurelpress.blogs.mapper;

import com.neurelpress.blogs.dao.Article;
import com.neurelpress.blogs.dto.response.ArticleResponse;
import com.neurelpress.blogs.dto.response.ArticleSummaryResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Slf4j
@Component
@Getter
@Setter
@RequiredArgsConstructor
public class ArticleMapper {

    private final UserMapper userMapper;
    private final TagMapper tagMapper;
    private final BookMapper bookMapper;

    public ArticleResponse toResponse(Article article) {
        return toResponse(article, null);
    }

    public ArticleResponse toResponse(Article article, Boolean bookmarked) {
        log.debug("ArticleMapper.toResponse: {}", article.getId());
        return new ArticleResponse(
                article.getId(),
                article.getTitle(),
                article.getSlug(),
                article.getSummary(),
                article.getContent(),
                article.getCoverImage(),
                article.getStatus().name(),
                article.getReadTime(),
                article.getClaps(),
                article.getBookmarksCount(),
                article.getCommentsCount(),
                article.getViews(),
                article.getSeoTitle(),
                article.getSeoDescription(),
                article.getCanonicalUrl(),
                article.getPublishedAt(),
                article.getCreatedAt(),
                article.getUpdatedAt(),
                userMapper.toAuthorSummary(article.getAuthor()),
                new ArrayList<>(tagMapper.toResponseSet(article.getTags())),
                new ArrayList<>(bookMapper.toResponseSet(article.getBooks())),
                bookmarked
        );
    }

    public ArticleSummaryResponse toSummaryResponse(Article article) {
        log.debug("ArticleMapper.toSummaryResponse: {}", article.getId());

        return new ArticleSummaryResponse(
                article.getId(),
                article.getTitle(),
                article.getSlug(),
                article.getSummary(),
                article.getCoverImage(),
                article.getStatus().name(),
                article.getReadTime(),
                (int) article.getViews(),
                article.getClaps(),
                article.getBookmarksCount(),
                article.getPublishedAt(),
                article.getCreatedAt(),
                userMapper.toAuthorSummary(article.getAuthor()),
                new ArrayList<>(tagMapper.toResponseSet(article.getTags()))
        );
    }
}
