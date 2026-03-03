package com.neurelpress.blogs.config;

import com.neurelpress.blogs.constants.CodeConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GeminiConfiguration {

    @Bean
    public WebClient geminiWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(CodeConstants.GEMINI_BASE_URL)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
