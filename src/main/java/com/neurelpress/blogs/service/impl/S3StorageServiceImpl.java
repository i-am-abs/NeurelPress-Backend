package com.neurelpress.blogs.service.impl;

import com.neurelpress.blogs.config.properties.NeuralPressS3Properties;
import com.neurelpress.blogs.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageServiceImpl implements StorageService {

    private final ObjectProvider<S3Client> s3ClientProvider;
    private final NeuralPressS3Properties properties;

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        S3Client s3Client = s3ClientProvider.getIfAvailable();
        if (!properties.isConfigured() || s3Client == null) {
            log.warn("S3 is not configured. Returning local placeholder URL.");
            return "https://assets.neuralpress.dev/placeholder.jpg"; // Placeholder if not configured
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String filename = (folder != null && !folder.isBlank() ? folder + "/" : "")
                    + UUID.randomUUID() + extension;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(filename)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Strip trailing slash from publicUrl if present, then append filename
            String publicUrl = properties.publicUrl();
            if (publicUrl.endsWith("/")) {
                publicUrl = publicUrl.substring(0, publicUrl.length() - 1);
            }

            return publicUrl + "/" + filename;

        } catch (IOException e) {
            log.error("Failed to upload file to S3", e);
            throw new RuntimeException("Failed to upload file to storage", e);
        }
    }
}
