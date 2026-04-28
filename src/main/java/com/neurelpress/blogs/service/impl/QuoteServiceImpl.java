package com.neurelpress.blogs.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neurelpress.blogs.dao.Quote;
import com.neurelpress.blogs.dto.response.QuoteResponse;
import com.neurelpress.blogs.repository.QuoteRepository;
import com.neurelpress.blogs.service.QuoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;
    private final RestClient geminiRestClient;
    private final ObjectMapper objectMapper;

    @Value("${neuralpress.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${neuralpress.gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "quoteOfTheDay")
    public QuoteResponse getQuoteOfTheDay() {
        List<Quote> active = quoteRepository.findAllActive();
        if (active.isEmpty()) {
            log.info("No active quotes configured; trying AI-generated quote.");
            QuoteResponse aiQuote = generateAiQuoteOfTheDay();
            if (aiQuote != null) {
                return aiQuote;
            }
            QuoteResponse fallback = fallbackQuoteOfTheDay();
            log.info("Using static fallback quote: {}", fallback.text());
            return fallback;
        }
        int dayOfYear = LocalDate.now().getDayOfYear();
        Quote quote = active.get(Math.floorMod(dayOfYear, active.size()));
        log.info("Quote of the day: {}", quote.getText());
        return new QuoteResponse(quote.getId(), quote.getText(), quote.getAuthor(), quote.getSource());
    }

    private QuoteResponse generateAiQuoteOfTheDay() {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            log.info("Gemini API key missing; skipping AI quote generation.");
            return null;
        }
        try {
            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of(
                                                    "text",
                                                    "Generate one short motivational quote for AI/LLM/RAG engineers. " +
                                                            "Return strictly JSON object with keys: text, author."
                                            )
                                    )
                            )
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.4,
                            "maxOutputTokens", 120
                    )
            );

            String response = geminiRestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/models/{model}:generateContent")
                            .queryParam("key", geminiApiKey)
                            .build(geminiModel))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            String generatedText = extractTextFromGemini(response);
            if (generatedText.isBlank()) {
                return null;
            }

            JsonNode candidate = objectMapper.readTree(generatedText);
            String text = candidate.path("text").asText("").trim();
            String author = candidate.path("author").asText("NeurelPress AI").trim();
            if (text.isBlank()) {
                return null;
            }
            return new QuoteResponse(null, text, author.isBlank() ? "NeurelPress AI" : author, "gemini");
        } catch (Exception ex) {
            log.warn("Failed generating AI quote: {}", ex.getMessage());
            return null;
        }
    }

    private String extractTextFromGemini(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            return text.asText("").trim();
        } catch (Exception ex) {
            log.warn("Failed parsing Gemini quote response: {}", ex.getMessage());
            return "";
        }
    }

    private QuoteResponse fallbackQuoteOfTheDay() {
        List<QuoteResponse> fallbackQuotes = List.of(
                new QuoteResponse(null, "RAG provides memory; reasoning gives direction.", "NeurelPress", "fallback"),
                new QuoteResponse(null, "Great LLM systems are not prompts alone; they are architecture.", "NeurelPress", "fallback"),
                new QuoteResponse(null, "First ground your model in facts, then optimize its voice.", "NeurelPress", "fallback")
        );
        int dayOfYear = LocalDate.now().getDayOfYear();
        return fallbackQuotes.get(Math.floorMod(dayOfYear, fallbackQuotes.size()));
    }
}
