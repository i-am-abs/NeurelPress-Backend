package com.neurelpress.blogs.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neurelpress.blogs.constants.CodeConstants;
import com.neurelpress.blogs.service.AiSuggestionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "neuralpress.ai.provider", havingValue = "mistral")
public class MistralAiSuggestionsService implements AiSuggestionsService {

    private final ObjectMapper objectMapper;
    @Qualifier("mistralRestClient")
    private final RestClient mistralRestClient;

    @Value("${neuralpress.mistral.api-key:}")
    private String apiKey;

    @Value("${neuralpress.mistral.model:mistral-small-latest}")
    private String model;

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max);
    }

    @Override
    public List<String> suggestTags(String title, String contentSnippet) {
        if (isConfigured()) {
            log.warn("Mistral API key not configured. Skipping tag suggestions.");
            return Collections.emptyList();
        }
        log.debug("Mistral suggesting tags");
        String prompt = CodeConstants.TAG_SUGGESTION_PROMPT.formatted(
                truncate(title, 300),
                truncate(contentSnippet, 1500)
        );
        String response = generateContent(prompt);
        return parseStringArray(response);
    }

    @Override
    public String suggestTitle(String contentSnippet) {
        if (isConfigured() || contentSnippet.isBlank()) return null;
        String prompt = CodeConstants.TITLE_SUGGESTION_PROMPT.formatted(truncate(contentSnippet, 2000));
        return generateContent(prompt);
    }

    @Override
    public String summarize(String contentSnippet) {
        if (isConfigured() || contentSnippet.isBlank()) return null;
        String prompt = CodeConstants.SUMMARY_PROMPT.formatted(truncate(contentSnippet, 4000));
        return generateContent(prompt);
    }

    private boolean isConfigured() {
        return apiKey == null || apiKey.isBlank();
    }

    @SuppressWarnings("unchecked")
    private String generateContent(String prompt) {
        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(Map.of("role", "user", "content", prompt)),
                    "max_tokens", 512,
                    "temperature", 0.3
            );
            String response = mistralRestClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            return extractTextFromResponse(response);
        } catch (Exception e) {
            log.warn("Mistral API call failed: {}", e.getMessage());
            return "";
        }
    }

    private String extractTextFromResponse(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode choices = root.path("choices");
            if (choices.isEmpty()) return "";
            JsonNode message = choices.get(0).path("message");
            return message.path("content").asText("").trim();
        } catch (Exception e) {
            log.warn("Failed parsing Mistral response");
            return "";
        }
    }

    private List<String> parseStringArray(String response) {
        if (response == null || response.isBlank()) {
            return Collections.emptyList();
        }

        String cleaned = response
                .replace("```json", "")
                .replace("```", "")
                .replace("[", "")
                .replace("]", "")
                .replace("`", "")
                .trim();

        if (cleaned.isEmpty()) {
            return Collections.emptyList();
        }

        String[] parts = cleaned.split("[,\\n]");

        return StreamSupport.stream(java.util.Arrays.spliterator(parts), false)
                .map(String::trim)
                .map(s -> s.replaceAll("^[-*\\d.\\s]+", "")) // drop bullets / numbering
                .map(s -> s.toLowerCase().replaceAll("[^a-z0-9\\s-]", "")) // keep alnum + spaces/hyphen
                .map(s -> s.replaceAll("\\s+", "-"))
                .filter(s -> !s.isEmpty())
                .distinct()
                .limit(5)
                .toList();
    }
}
