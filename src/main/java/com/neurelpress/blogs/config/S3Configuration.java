package com.neurelpress.blogs.config;

import com.neurelpress.blogs.config.properties.NeuralPressS3Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Slf4j
@Configuration
public class S3Configuration {

    @Bean
    public S3Client s3Client(NeuralPressS3Properties properties) {
        if (!properties.isConfigured()) {
            return null;
        }

        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                properties.accessKey(),
                properties.secretKey()
        );

        try {
            return S3Client.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .endpointOverride(URI.create(properties.endpoint()))
                    .region(Region.US_EAST_1)
                    .build();
        } catch (IllegalArgumentException ex) {
            log.warn("S3 endpoint is invalid ({}). Falling back to non-S3 storage.", properties.endpoint());
            return null;
        }
    }
}
