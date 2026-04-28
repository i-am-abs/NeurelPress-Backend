package com.neurelpress.blogs.controller;

import com.neurelpress.blogs.constants.ApiConstants;
import com.neurelpress.blogs.service.FeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.Api_Feed)
@Tag(name = "Feed", description = "RSS feed endpoint")
public class FeedController {

    private final FeedService feedService;

    @GetMapping(value = ApiConstants.Really_Simple_Syndication, produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Get RSS feed of latest articles")
    public ResponseEntity<String> rssFeed() {
        log.info("Generated Really Simple Syndication feed");
        return ResponseEntity.ok(feedService.generateRssFeed());
    }
}
