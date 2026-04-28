package com.neurelpress.blogs.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    /**
     * Uploads an image to the configured storage and returns its public URL.
     *
     * @param file   The file to upload.
     * @param folder The folder path (e.g. "covers").
     * @return The public URL of the uploaded image.
     */
    String uploadImage(MultipartFile file, String folder);
}
