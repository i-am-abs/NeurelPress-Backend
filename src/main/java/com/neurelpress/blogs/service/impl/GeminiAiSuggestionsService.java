package com.neurelpress.blogs.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neurelpress.blogs.constants.CodeConstants;
import com.neurelpress.blogs.service.AiSuggestionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "neuralpress.ai.provider", havingValue = "gemini", matchIfMissing = true)
public class GeminiAiSuggestionsService implements AiSuggestionsService {

    private final RestClient geminiRestClient;
    private final ObjectMapper objectMapper;

    @Value("${neuralpress.gemini.api-key:}")
    private String apiKey;

    @Value("${neuralpress.gemini.model:gemini-1.5-flash}")
    private String model;

    private static String truncate(String s, int max) {

        if (s == null) {
            return "";
        }

        return s.length() <= max ? s : s.substring(0, max);
    }

    @Override
    public List<String> suggestTags(String title, String contentSnippet) {

        if (isConfigured()) {
            log.warn("Gemini API key not configured. Skipping tag suggestions.");
            return Collections.emptyList();
        }

        String prompt = CodeConstants.TAG_SUGGESTION_PROMPT.formatted(
                truncate(title, 300),
                truncate(contentSnippet, 1500)
        );

        String response = generateContent(prompt);

        log.debug("Gemini tag suggestion response: {}", response);

        return parseStringArray(response);
    }

    @Override
    public String suggestTitle(String contentSnippet) {

        if (isConfigured() || contentSnippet.isBlank()) {
            return null;
        }

        String prompt = CodeConstants.TITLE_SUGGESTION_PROMPT.formatted(
                truncate(contentSnippet, 2000)
        );

        return generateContent(prompt);
    }

    @Override
    public String summarize(String contentSnippet) {

        if (isConfigured() || contentSnippet.isBlank()) {
            return null;
        }

        String prompt = CodeConstants.SUMMARY_PROMPT.formatted(
                truncate(contentSnippet, 4000)
        );

        return generateContent(prompt);
    }

    private boolean isConfigured() {
        return apiKey == null || apiKey.isBlank();
    }

    private String generateContent(String prompt) {
        try {

            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.3,
                            "maxOutputTokens", 512
                    )
            );

            String response = geminiRestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/models/{model}:generateContent")
                            .queryParam("key", apiKey)
                            .build(model))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return extractTextFromResponse(response);

        } catch (Exception e) {

            log.warn("Gemini API call failed: {}", e.getMessage());

            return "";
        }
    }

    private String extractTextFromResponse(String json) {

        try {

            JsonNode root = objectMapper.readTree(json);

            JsonNode candidates = root.path("candidates");

            if (candidates.isEmpty()) {
                return "";
            }

            JsonNode parts = candidates
                    .get(0)
                    .path("content")
                    .path("parts");

            if (parts.isEmpty()) {
                return "";
            }

            return parts
                    .get(0)
                    .path("text")
                    .asText("")
                    .trim();

        } catch (Exception e) {

            log.warn("Failed parsing Gemini response");

            return "";
        }
    }

    private List<String> parseStringArray(String response) {

        if (response == null || response.isBlank()) {
            return Collections.emptyList();
        }

        try {

            String cleaned = response
                    .replaceAll("^\\s*\\[?|\\]?\\s*$", "")
                    .trim();

            if (cleaned.isEmpty()) {
                return Collections.emptyList();
            }

            JsonNode arr = objectMapper.readTree("[" + cleaned + "]");

            return StreamSupport.stream(arr.spliterator(), false)
                    .map(JsonNode::asText)
                    .map(s -> s
                            .replaceAll("^\"|\"$", "")
                            .trim()
                            .toLowerCase()
                            .replaceAll("\\s+", "-"))
                    .filter(s -> !s.isEmpty())
                    .limit(5)
                    .toList();

        } catch (Exception e) {

            log.warn("Failed parsing tag array: {}", e.getMessage());

            return Collections.emptyList();
        }
    }
}
