package com.neurelpress.blogs;

import com.neurelpress.blogs.config.properties.NeuralPressGoogleProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableCaching
@ConfigurationPropertiesScan
@SpringBootApplication
@EnableConfigurationProperties(NeuralPressGoogleProperties.class)
public class
NeuralPressBloggingAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(NeuralPressBloggingAppApplication.class, args);
    }
}
