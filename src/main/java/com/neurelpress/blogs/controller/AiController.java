package com.neurelpress.blogs.controller;

import com.neurelpress.blogs.constants.ApiConstants;
import com.neurelpress.blogs.constants.CodeConstants;
import com.neurelpress.blogs.security.UserPrincipal;
import com.neurelpress.blogs.service.AiSuggestionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.Api_Ai)
@Tag(name = "AI", description = "Gemini-powered suggestions (tags, title, summary)")
public class AiController {

    private final AiSuggestionsService aiSuggestionsService;

    @PostMapping(ApiConstants.Suggested_Tags)
    @Operation(summary = "Suggest tags for an article")
    public ResponseEntity<List<String>> suggestTags(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody Map<String, String> body) {
        String title = body.getOrDefault(CodeConstants.TITLE, "");
        String content = body.getOrDefault(CodeConstants.CONTENT, "");
        String snippet = content.length() > 1500 ? content.substring(0, 1500) : content;
        log.info("Suggesting tags for article: title={}, snippet={}", title, snippet);
        return ResponseEntity.ok(aiSuggestionsService.suggestTags(title, snippet));
    }

    @PostMapping(ApiConstants.Suggested_Title)
    @Operation(summary = "Suggest a title for content")
    public ResponseEntity<Map<String, String>> suggestTitle(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody Map<String, String> body) {
        String content = body.getOrDefault(CodeConstants.CONTENT, "");
        String title = aiSuggestionsService.suggestTitle(content);
        log.info("Suggested title: {}", title);
        return ResponseEntity.ok(Map.of(CodeConstants.TITLE, title != null ? title : ""));
    }

    @PostMapping(ApiConstants.Suggested_Summary)
    @Operation(summary = "Suggest meta summary for content")
    public ResponseEntity<Map<String, String>> suggestSummary(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody Map<String, String> body) {
        String content = body.getOrDefault(CodeConstants.CONTENT, "");
        String summary = aiSuggestionsService.summarize(content);
        log.info("Suggested summary: {}", summary);
        return ResponseEntity.ok(Map.of(CodeConstants.SUMMARY, summary != null ? summary : ""));
    }

    @PostMapping(ApiConstants.Humanize)
    @Operation(summary = "Humanize AI-generated content")
    public ResponseEntity<Map<String, String>> humanize(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody Map<String, String> body) {
        String content = body.getOrDefault(CodeConstants.CONTENT, "");
        String humanized = aiSuggestionsService.humanize(content);
        return ResponseEntity.ok(Map.of(CodeConstants.CONTENT, humanized != null ? humanized : ""));
    }

    @PostMapping(ApiConstants.Analyze_Tone)
    @Operation(summary = "Analyze content tone")
    public ResponseEntity<Map<String, Object>> analyzeTone(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody Map<String, String> body) {
        String content = body.getOrDefault(CodeConstants.CONTENT, "");
        return ResponseEntity.ok(aiSuggestionsService.analyzeTone(content));
    }

    @PostMapping(ApiConstants.Generate_By_Tone)
    @Operation(summary = "Generate content in a chosen tone")
    public ResponseEntity<Map<String, String>> generateByTone(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody Map<String, String> body) {
        String content = body.getOrDefault(CodeConstants.CONTENT, "");
        String tone = body.getOrDefault("tone", "professional");
        String rewritten = aiSuggestionsService.generateByTone(content, tone);
        return ResponseEntity.ok(Map.of(CodeConstants.CONTENT, rewritten != null ? rewritten : ""));
    }
}
