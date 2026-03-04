package com.neurelpress.blogs.service;


import com.neurelpress.blogs.dto.request.ArticleRequest;
import com.neurelpress.blogs.dto.response.ArticleResponse;
import com.neurelpress.blogs.dto.response.ArticleSummaryResponse;
import com.neurelpress.blogs.dto.response.PageResponse;

import java.util.UUID;

public interface ArticleService {

    ArticleResponse createArticle(UUID authorId, ArticleRequest request);

    ArticleResponse updateArticle(UUID authorId, String slug, ArticleRequest request);

    ArticleResponse publishArticle(UUID authorId, String slug);

    ArticleResponse getArticleBySlug(String slug);

    void recordView(String slug);

    PageResponse<ArticleSummaryResponse> getPublishedArticles(int page, int size);

    PageResponse<ArticleSummaryResponse> getArticlesByTag(String tagSlug, int page, int size);

    PageResponse<ArticleSummaryResponse> getArticlesByAuthor(UUID authorId, int page, int size);

    PageResponse<ArticleSummaryResponse> getDraftsByAuthor(UUID authorId, int page, int size);

    void deleteArticle(UUID authorId, String slug);

    void clapArticle(UUID userId, String slug);
}
