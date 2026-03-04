package com.neurelpress.blogs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@ConditionalOnProperty(name = "neuralpress.ai.provider", havingValue = "mistral")
public class MistralConfiguration {

    @Bean(name = "mistralRestClient")
    public RestClient mistralRestClient(@Value("${neuralpress.mistral.api-key:}") String apiKey) {
        return RestClient.builder()
                .baseUrl("https://api.mistral.ai/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
