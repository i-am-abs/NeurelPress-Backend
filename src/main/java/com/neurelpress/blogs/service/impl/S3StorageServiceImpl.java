package com.neurelpress.blogs.service.impl;

import com.neurelpress.blogs.dto.properties.NeuralPressS3Properties;
import com.neurelpress.blogs.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageServiceImpl implements StorageService {
    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1920;
    private static final float WEBP_QUALITY = 0.84f;
    private static final float JPEG_QUALITY = 0.88f;
    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;

    private final ObjectProvider<S3Client> s3ClientProvider;
    private final NeuralPressS3Properties properties;

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        S3Client s3Client = s3ClientProvider.getIfAvailable();
        if (properties.isConfigured() || s3Client == null) {
            log.warn("S3 is not configured. Returning local placeholder URL.");
            return "https://assets.neuralpress.dev/placeholder.jpg";
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("Image exceeds 10MB limit");
        }

        try {
            OptimizedImage optimizedImage = optimizeImage(file);

            String filename = (folder != null && !folder.isBlank() ? folder + "/" : "")
                    + UUID.randomUUID() + optimizedImage.extension();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(filename)
                    .contentType(optimizedImage.contentType())
                    .cacheControl("public, max-age=31536000, immutable")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(optimizedImage.data()));

            String publicUrl = properties.publicUrl();
            if (publicUrl.endsWith("/")) {
                publicUrl = publicUrl.substring(0, publicUrl.length() - 1);
            }

            return publicUrl + "/" + filename;

        } catch (IOException e) {
            log.error("Failed to upload image to Cloudflare R2", e);
            throw new RuntimeException("Failed to upload file to storage", e);
        }
    }

    @Contract("_ -> new")
    private @NonNull OptimizedImage optimizeImage(@NonNull MultipartFile file) throws IOException {
        BufferedImage source;
        try (InputStream inputStream = file.getInputStream()) {
            source = ImageIO.read(inputStream);
        }
        if (source == null) {
            throw new IllegalArgumentException("Unsupported image format");
        }

        BufferedImage resized = resizeIfNeeded(source);
        BufferedImage rgb = new BufferedImage(resized.getWidth(), resized.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgb.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
        graphics.drawImage(resized, 0, 0, null);
        graphics.dispose();

        if (hasWebpWriter()) {
            try {
                return new OptimizedImage(writeImage(rgb, "webp", WEBP_QUALITY), ".webp", "image/webp");
            } catch (Exception ex) {
                log.warn("WebP encoding unavailable, falling back to JPEG: {}", ex.getMessage());
            }
        }

        return new OptimizedImage(writeImage(rgb, "jpeg", JPEG_QUALITY), ".jpg", "image/jpeg");
    }

    private @NonNull BufferedImage resizeIfNeeded(@NonNull BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (width <= MAX_WIDTH && height <= MAX_HEIGHT) {
            return image;
        }

        double ratio = Math.min((double) MAX_WIDTH / width, (double) MAX_HEIGHT / height);
        int targetWidth = Math.max(1, (int) Math.round(width * ratio));
        int targetHeight = Math.max(1, (int) Math.round(height * ratio));

        BufferedImage output = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = output.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();
        return output;
    }

    private byte @NonNull [] writeImage(BufferedImage image, String format, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
        if (!writers.hasNext()) {
            throw new IllegalStateException("No image writer available for format: " + format);
        }

        ImageWriter writer = writers.next();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream)) {
            writer.setOutput(imageOutputStream);
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            if (writeParam.canWriteCompressed()) {
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                writeParam.setCompressionQuality(quality);
            }
            writer.write(null, new IIOImage(image, null, null), writeParam);
        } finally {
            writer.dispose();
        }
        return outputStream.toByteArray();
    }

    private boolean hasWebpWriter() {
        return ImageIO.getImageWritersByFormatName("webp").hasNext();
    }

    private record OptimizedImage(byte[] data, String extension, String contentType) {
    }
}
