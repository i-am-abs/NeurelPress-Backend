package com.neurelpress.blogs.config.properties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "neuralpress.mail")
public record NeuralPressMailProperties(

        @NotBlank
        @Email(message = "neuralpress.mail.from must be a valid email address")
        String from
) {
}
