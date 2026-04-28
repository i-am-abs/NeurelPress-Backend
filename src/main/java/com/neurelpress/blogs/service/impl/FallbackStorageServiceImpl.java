package com.neurelpress.blogs.service.impl;

import com.neurelpress.blogs.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@ConditionalOnMissingBean(StorageService.class)
public class FallbackStorageServiceImpl implements StorageService {

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        log.warn("S3 not configured, using fallback upload URL for folder={}", folder);
        return "https://assets.neuralpress.dev/placeholder.jpg";
    }
}
