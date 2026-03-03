package com.neurelpress.blogs.mapper;

import com.neurelpress.blogs.dto.response.ArticleResponse;
import com.neurelpress.blogs.dto.response.ArticleSummaryResponse;
import com.neurelpress.blogs.dao.Article;
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
        log.info("ArticleMapper.toResponse: {}", article);

        return new ArticleResponse(
                article.getId(),
                article.getTitle(),
                article.getSlug(),
                article.getSummary(),
                article.getContent(),
                article.getCoverImage(),
                article.getStatus().name(),
                article.getReadTime(),
                (int) article.getViews(),
                article.getClaps(),
                article.getBookmarksCount(),
                article.getCommentsCount(),
                article.getSeoTitle(),
                article.getSeoDescription(),
                article.getCanonicalUrl(),
                article.getPublishedAt(),
                article.getCreatedAt(),
                article.getUpdatedAt(),
                userMapper.toAuthorSummary(article.getAuthor()),
                new ArrayList<>(tagMapper.toResponseSet(article.getTags())),
                new ArrayList<>(bookMapper.toResponseSet(article.getBooks()))
        );
    }

    public ArticleSummaryResponse toSummaryResponse(Article article) {
        log.info("ArticleMapper.toSummaryResponse: {}", article);

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
