package com.neurelpress.blogs.controller;

import com.neurelpress.blogs.constants.ApiConstants;
import com.neurelpress.blogs.dto.request.ArticleRequest;
import com.neurelpress.blogs.dto.response.ArticleResponse;
import com.neurelpress.blogs.dto.response.ArticleSummaryResponse;
import com.neurelpress.blogs.dto.response.PageResponse;
import com.neurelpress.blogs.security.UserPrincipal;
import com.neurelpress.blogs.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.Api_Articles)
@Tag(name = "Articles", description = "Article management endpoints")
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping(value = {ApiConstants.Latest})
    @Operation(summary = "Get published articles (paginated)")
    public ResponseEntity<PageResponse<ArticleSummaryResponse>> getArticles(@RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "12") int size) {
        log.info("Getting articles with page {} and size {}", page, size);
        return ResponseEntity.ok(articleService.getPublishedArticles(page, size));
    }

    @GetMapping(ApiConstants.Me_Drafts)
    @Operation(summary = "Get current user's drafts")
    public ResponseEntity<PageResponse<ArticleSummaryResponse>> getMyDrafts(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                            @RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "12") int size) {
        log.info("Getting drafts for user {} with page {} and size {}", userPrincipal.getId(), page, size);
        return ResponseEntity.ok(articleService.getDraftsByAuthor(userPrincipal.getId(), page, size));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get a published article by slug")
    public ResponseEntity<ArticleResponse> getArticle(@PathVariable String slug) {
        articleService.recordView(slug);
        log.info("Article viewed: {}", slug);
        return ResponseEntity.ok(articleService.getArticleBySlug(slug));
    }

    @GetMapping("/tag/{tagSlug}")
    @Operation(summary = "Get articles by tag")
    public ResponseEntity<PageResponse<ArticleSummaryResponse>> getByTag(@PathVariable String tagSlug,
                                                                         @RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "12") int size) {
        log.info("Getting articles by tag {} with page {} and size {}", tagSlug, page, size);
        return ResponseEntity.ok(articleService.getArticlesByTag(tagSlug, page, size));
    }

    @GetMapping("/author/{authorId}")
    @Operation(summary = "Get published articles by author")
    public ResponseEntity<PageResponse<ArticleSummaryResponse>> getByAuthor(@PathVariable UUID authorId,
                                                                            @RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "12") int size) {
        log.info("Getting articles by author {} with page {} and size {}", authorId, page, size);
        return ResponseEntity.ok(articleService.getArticlesByAuthor(authorId, page, size));
    }

    @PostMapping
    @Operation(summary = "Create a new article (draft)")
    public ResponseEntity<ArticleResponse> createArticle(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                         @Valid @RequestBody ArticleRequest request) {
        log.info("Creating article: {}", request);
        return ResponseEntity.status(HttpStatus.CREATED).body(articleService.createArticle(userPrincipal.getId(), request));
    }

    @PutMapping("/{slug}")
    @Operation(summary = "Update an article")
    public ResponseEntity<ArticleResponse> updateArticle(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                         @PathVariable String slug,
                                                         @Valid @RequestBody ArticleRequest request) {
        log.info("Updating article: {}", request);
        return ResponseEntity.ok(articleService.updateArticle(userPrincipal.getId(), slug, request));
    }

    @PostMapping("/{slug}/publish")
    @Operation(summary = "Publish a draft article")
    public ResponseEntity<ArticleResponse> publishArticle(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                          @PathVariable String slug) {
        log.info("Publishing article: {}", slug);
        return ResponseEntity.ok(articleService.publishArticle(userPrincipal.getId(), slug));
    }

    @PostMapping("/{slug}/clap")
    @Operation(summary = "Clap for an article (one clap per user)")
    public ResponseEntity<Void> clapArticle(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String slug) {
        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        articleService.clapArticle(userPrincipal.getId(), slug);
        log.info("Clapped for article: {}", slug);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{slug}")
    @Operation(summary = "Delete an article")
    public ResponseEntity<Void> deleteArticle(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                              @PathVariable String slug) {
        articleService.deleteArticle(userPrincipal.getId(), slug);
        log.info("Deleted article: {}", slug);
        return ResponseEntity.noContent().build();
    }
}
