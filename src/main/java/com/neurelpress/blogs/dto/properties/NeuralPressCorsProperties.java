package com.neurelpress.blogs.dto.properties;

import jakarta.validation.constraints.NotBlank;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "neuralpress")
public record NeuralPressCorsProperties(
        Cors cors,
        App app
) {
    public record Cors(
            @NotBlank String allowedOrigins
    ) {
        @Contract(pure = true)
        public String @NonNull [] allowedOriginsArray() {
            return allowedOrigins.split(",");
        }
    }

    public record App(
            @NotBlank String frontendUrl
    ) {
        public @NonNull String primaryFrontendUrl() {
            return frontendUrl.split(",")[0].trim();
        }
    }
}
