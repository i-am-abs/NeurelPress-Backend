package com.neurelpress.blogs.dto.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "s3")
public record NeuralPressS3Properties(
        String endpoint,
        String accessKey,
        String secretKey,
        String bucket,
        String publicUrl
) {
    public boolean isConfigured() {
        return isUsable(endpoint)
                || isUsable(accessKey)
                || isUsable(secretKey)
                || isUsable(bucket);
    }

    private boolean isUsable(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return value.startsWith("${") && value.endsWith("}");
    }
}
