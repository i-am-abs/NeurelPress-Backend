package com.neurelpress.blogs.controller;

import com.neurelpress.blogs.constants.ApiConstants;
import com.neurelpress.blogs.dto.response.TagResponse;
import com.neurelpress.blogs.mapper.TagMapper;
import com.neurelpress.blogs.repository.TagRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(ApiConstants.Api_Tags)
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Tag endpoints")
public class TagController {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @GetMapping
    @Operation(summary = "Get all tags")
    @Cacheable(cacheNames = "allTags")
    public ResponseEntity<List<TagResponse>> getAllTags() {
        log.info("Getting all tags");
        return ResponseEntity.ok(
                tagRepository.findAll()
                        .stream()
                        .map(tagMapper::toResponse)
                        .toList()
        );
    }

    @GetMapping(ApiConstants.Top)
    @Operation(summary = "Get top tags by article count")
    @Cacheable(cacheNames = "topTags", key = "#limit")
    public ResponseEntity<List<TagResponse>> getTopTags(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Getting top tags with limit: {}", limit);
        return ResponseEntity.ok(
                tagRepository.findTopTags(PageRequest.of(0, limit))
                        .stream()
                        .map(tagMapper::toResponse)
                        .toList()
        );
    }
}
