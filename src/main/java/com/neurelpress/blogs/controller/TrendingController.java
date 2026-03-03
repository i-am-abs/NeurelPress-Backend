package com.neurelpress.blogs.controller;

import com.neurelpress.blogs.constants.ApiConstants;
import com.neurelpress.blogs.dto.response.ArticleSummaryResponse;
import com.neurelpress.blogs.service.TrendingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.Api_Trending)
@Tag(name = "Trending", description = "Trending articles endpoints")
public class TrendingController {

    private final TrendingService trendingService;

    @GetMapping
    @Operation(summary = "Get trending articles")
    public ResponseEntity<List<ArticleSummaryResponse>> getTrending(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Getting trending articles with limit: {}", limit);
        return ResponseEntity.ok(trendingService.getTrendingArticles(limit));
    }
}
