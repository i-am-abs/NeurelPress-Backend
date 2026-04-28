package com.neurelpress.blogs.config.properties;

import jakarta.validation.constraints.NotBlank;
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
        public String[] allowedOriginsArray() {
            return allowedOrigins.split(",");
        }
    }

    public record App(
            @NotBlank String frontendUrl
    ) {
        public String primaryFrontendUrl() {
            return frontendUrl.split(",")[0].trim();
        }
    }
}
