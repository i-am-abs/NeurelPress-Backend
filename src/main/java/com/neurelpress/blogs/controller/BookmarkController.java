package com.neurelpress.blogs.controller;

import com.neurelpress.blogs.constants.ApiConstants;
import com.neurelpress.blogs.dto.response.ArticleSummaryResponse;
import com.neurelpress.blogs.dto.response.PageResponse;
import com.neurelpress.blogs.security.UserPrincipal;
import com.neurelpress.blogs.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.Api_Bookmarks)
@Tag(name = "Bookmarks", description = "Bookmark management endpoints")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/{slug}")
    @Operation(summary = "Toggle bookmark on an article")
    public ResponseEntity<Void> toggleBookmark(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String slug) {
        bookmarkService.toggleBookmark(userPrincipal.getId(), slug);
        log.info("Bookmark toggled for user: {} on article: {}", userPrincipal.getId(), slug);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "Get current user's bookmarks")
    public ResponseEntity<PageResponse<ArticleSummaryResponse>> getBookmarks(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        log.info("Getting bookmarks for user: {} with page: {} and size: {}", userPrincipal.getId(), page, size);
        return ResponseEntity.ok(bookmarkService.getUserBookmarks(userPrincipal.getId(), page, size));
    }
}
