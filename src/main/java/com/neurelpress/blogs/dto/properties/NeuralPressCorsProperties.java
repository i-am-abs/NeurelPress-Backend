package com.neurelpress.blogs.dto.properties;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.stream.Stream;

@Validated
@ConfigurationProperties(prefix = "neuralpress")
public record NeuralPressCorsProperties(
        Cors cors,
        App app
) {
    public record Cors(
            String allowedOrigins,
            String allowedOriginPatterns
    ) {
        @Contract(pure = true)
        public String @NonNull [] allowedOriginsArray() {
            return splitCsv(allowedOrigins);
        }

        @Contract(pure = true)
        public String @NonNull [] allowedOriginPatternsArray() {
            return splitCsv(allowedOriginPatterns);
        }

        private static String @NonNull [] splitCsv(String raw) {
            if (!StringUtils.hasText(raw)) {
                return new String[0];
            }
            return Stream.of(raw.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .toArray(String[]::new);
        }
    }

    public record App(
            String frontendUrl
    ) {
        public @NonNull String primaryFrontendUrl() {
            if (!StringUtils.hasText(frontendUrl)) {
                return "http://localhost:3000";
            }
            return frontendUrl.split(",")[0].trim();
        }
    }
}
