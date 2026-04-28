package com.neurelpress.blogs.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "google")
public record NeuralPressGoogleProperties(

        @NotBlank(message = "google.client-id must be configured for Google One Tap to work")
        String clientId,

        String clientSecret
) {
}
