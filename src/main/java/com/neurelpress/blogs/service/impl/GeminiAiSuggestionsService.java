package com.neurelpress.blogs.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neurelpress.blogs.service.AiSuggestionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAiSuggestionsService implements AiSuggestionsService {

    private final WebClient geminiWebClient;
    private final ObjectMapper objectMapper;

    @Value("${neuralpress.gemini.api-key:}")
    private String apiKey;

    @Value("${neuralpress.gemini.model:gemini-1.5-flash}")
    private String model;

    @Override
    public List<String> suggestTags(String title, String contentSnippet) {
        if (isConfigured()) {
            return Collections.emptyList();
        }

        String prompt = """
            You are an expert at tagging technical and AI/ML articles for a scientific publishing platform.
            Given the following article title and a short excerpt, suggest exactly 3-5 relevant tags.
            Return ONLY a JSON array of tag names (lowercase, hyphenated for multi-word, e.g. "machine-learning").
            No explanation. Example: ["deep-learning","nlp","transformers"]
            Title: %s
            Excerpt: %s
            """.formatted(
                truncate(title, 300),
                truncate(contentSnippet, 1500)
            );

        String response = generateContent(prompt);
        log.info("Suggested tags: {}", response);
        return parseStringArray(response);
    }

    @Override
    public String suggestTitle(String contentSnippet) {
        if (isConfigured()) {
            return null;
        }

        if (contentSnippet.isBlank()) {
            return null;
        }

        String prompt = """
            You are an editor for a technical blog. Suggest one concise, SEO-friendly title (max 80 chars) for this article.
            Return ONLY the title, no quotes or explanation.
            Excerpt: %s
            """.formatted(truncate(contentSnippet, 2000));

        log.info("Suggested title: {}", prompt);
        return generateContent(prompt);
    }

    @Override
    public String summarize(String contentSnippet) {
        if (isConfigured()) {
            return null;
        }

        if (contentSnippet.isBlank()) {
            return null;
        }

        String prompt = """
            Summarize this technical article in 1-2 sentences for a meta description (max 160 chars). Be concise.
            Return ONLY the summary, no quotes.
            Content: %s
            """.formatted(truncate(contentSnippet, 4000));

        log.info("Summarized content: {}", prompt);
        return generateContent(prompt);
    }

    private boolean isConfigured() {
        return apiKey == null || apiKey.isBlank();
    }

    @SuppressWarnings("unchecked")
    private String generateContent(String prompt) {
        try {
            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                    "generationConfig", Map.of("temperature", 0.3, "maxOutputTokens", 512)
            );

            String response = geminiWebClient.post()
                    .uri(uri -> uri.path("/models/{model}:generateContent").queryParam("key", apiKey).build(model))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("Gemini API response: {}", response);
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
            JsonNode content = candidates.get(0).path("content").path("parts");
            if (content.isEmpty()) {
                return "";
            }
            log.info("Extracted text: {}", content.get(0).path("text").asText(""));
            return content.get(0).path("text").asText("").trim();
        } catch (Exception e) {
            return "";
        }
    }

    private List<String> parseStringArray(String response) {
        if (response == null || response.isBlank()) {
            return Collections.emptyList();
        }
        try {
            String cleaned = response.replaceAll("^\\s*\\[?|\\]?\\s*$", "").trim();
            if (cleaned.isEmpty()) {
                return Collections.emptyList();
            }
            JsonNode arr = objectMapper.readTree("[" + cleaned + "]");
            log.info("Parsed JSON array: {}", arr);
            return StreamSupport.stream(arr.spliterator(), false)
                    .map(JsonNode::asText)
                    .map(s -> s.replaceAll("^\"|\"$", "").trim().toLowerCase().replaceAll("\\s+", "-"))
                    .filter(s -> !s.isEmpty())
                    .limit(5)
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to parse JSON array: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
