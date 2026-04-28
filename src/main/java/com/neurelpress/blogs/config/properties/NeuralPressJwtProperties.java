package com.neurelpress.blogs.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "neuralpress.jwt")
public record NeuralPressJwtProperties(

        @NotBlank(message = "JWT secret must not be blank")
        String secret,

        @Min(value = 60_000, message = "Access token must be valid for at least 60 seconds")
        long accessExpirationMs,

        @Min(value = 3_600_000, message = "Refresh token must be valid for at least 1 hour")
        long refreshExpirationMs
) {
}
