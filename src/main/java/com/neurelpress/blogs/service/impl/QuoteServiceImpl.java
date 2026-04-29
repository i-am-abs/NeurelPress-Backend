package com.neurelpress.blogs.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neurelpress.blogs.dto.properties.NeuralPressAiProperties;
import com.neurelpress.blogs.dao.Quote;
import com.neurelpress.blogs.dto.response.QuoteResponse;
import com.neurelpress.blogs.repository.QuoteRepository;
import com.neurelpress.blogs.service.QuoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;
    private final RestClient geminiRestClient;
    private final ObjectMapper objectMapper;
    private final NeuralPressAiProperties aiProperties;

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

    @Override
    public QuoteResponse getRandomQuote() {
        return generateAiQuote("ai-engineering", false);
    }

    @Override
    public QuoteResponse getRandomQuoteByDomain(String domain) {
        return generateAiQuote(normalizeDomain(domain), false);
    }

    @Override
    public QuoteResponse getTechPunchlineByDomain(String domain) {
        return generateAiQuote(normalizeDomain(domain), true);
    }

    @Override
    public QuoteResponse getFrontScreenQuote() {
        List<String> domains = List.of("ml", "dl", "rag", "llm", "crew ai", "mlops");
        String domain = domains.get(Math.floorMod((int) (System.nanoTime() ^ UUID.randomUUID().hashCode()), domains.size()));
        boolean punchline = Math.floorMod((int) (System.currentTimeMillis() ^ domain.hashCode()), 2) == 0;
        return generateAiQuote(domain, punchline);
    }

    private @Nullable QuoteResponse generateAiQuoteOfTheDay() {
        if (aiProperties.gemini().isConfigured()) {
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
                            .queryParam("key", aiProperties.gemini().apiKey())
                            .build(aiProperties.gemini().model()))
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

    private QuoteResponse generateAiQuote(String domain, boolean punchline) {
        if (!aiProperties.gemini().isConfigured()) {
            try {
                String prompt = buildPrompt(domain, punchline);
                String generatedText = callGemini(prompt);
                if (!generatedText.isBlank()) {
                    JsonNode candidate = objectMapper.readTree(generatedText);
                    String text = candidate.path("text").asText("").trim();
                    String author = candidate.path("author").asText("NeurelPress AI").trim();
                    if (!text.isBlank()) {
                        return new QuoteResponse(null, text, author.isBlank() ? "NeurelPress AI" : author, "gemini");
                    }
                }
            } catch (Exception ex) {
                log.warn("Failed generating AI quote/punchline for domain {}: {}", domain, ex.getMessage());
            }
        }
        return fallbackRandomQuote(domain, punchline);
    }

    private String callGemini(String prompt) {
        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.85,
                        "maxOutputTokens", 160
                )
        );

        String response = geminiRestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/models/{model}:generateContent")
                        .queryParam("key", aiProperties.gemini().apiKey())
                        .build(aiProperties.gemini().model()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        return extractTextFromGemini(response);
    }

    private String buildPrompt(String domain, boolean punchline) {
        if (punchline) {
            return "Generate one crisp tech punchline for " + domain + ". " +
                    "Tone: witty, insightful, engineer-friendly. Max 20 words. " +
                    "Return strictly JSON object with keys: text, author.";
        }
        return "Generate one short inspirational quote for " + domain + " engineers. " +
                "Focus areas can include ML, DL, RAG, LLM, CrewAI, and MLOps. Max 28 words. " +
                "Return strictly JSON object with keys: text, author.";
    }

    private String normalizeDomain(String domain) {
        if (domain == null || domain.isBlank()) {
            return "ai-engineering";
        }
        return domain.trim().toLowerCase();
    }

    private QuoteResponse fallbackRandomQuote(String domain, boolean punchline) {
        List<QuoteResponse> fallback = punchline
                ? List.of(
                new QuoteResponse(null, "In " + domain + ", latency is UX debt with interest.", "NeurelPress", "fallback"),
                new QuoteResponse(null, "Ship your " + domain + " pipeline before your slides about it.", "NeurelPress", "fallback"),
                new QuoteResponse(null, "Great " + domain + " starts where vague requirements end.", "NeurelPress", "fallback")
        )
                : List.of(
                new QuoteResponse(null, "In " + domain + ", reliable systems beat impressive demos.", "NeurelPress", "fallback"),
                new QuoteResponse(null, "Every strong " + domain + " product begins with clean feedback loops.", "NeurelPress", "fallback"),
                new QuoteResponse(null, "Progress in " + domain + " is measured by value delivered, not tokens consumed.", "NeurelPress", "fallback")
        );
        int idx = Math.floorMod((int) (System.nanoTime() ^ UUID.randomUUID().hashCode()), fallback.size());
        return fallback.get(idx);
    }

    private @NonNull String extractTextFromGemini(String response) {
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
