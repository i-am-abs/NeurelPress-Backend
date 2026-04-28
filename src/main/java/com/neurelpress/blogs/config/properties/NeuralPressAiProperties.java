package com.neurelpress.blogs.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "neuralpress")
public record NeuralPressAiProperties(
        String aiProvider,
        Gemini gemini) {
    public record Gemini(String apiKey, String model) {
        public boolean isConfigured() {
            return apiKey != null && !apiKey.isBlank();
        }
    }

    public boolean isGemini() {
        return "gemini".equalsIgnoreCase(effectiveProvider());
    }

    private String effectiveProvider() {
        if (aiProvider == null) {
            return "gemini";
        }
        String normalized = aiProvider.trim();
        if (normalized.isEmpty() || "null".equalsIgnoreCase(normalized)) {
            return "gemini";
        }
        return normalized;
    }
}
