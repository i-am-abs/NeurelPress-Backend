package com.neurelpress.blogs.config;

import com.neurelpress.blogs.constants.CodeConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GeminiConfiguration {

    @Bean
    public RestClient geminiRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(CodeConstants.GEMINI_BASE_URL)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}