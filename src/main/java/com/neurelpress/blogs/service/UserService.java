package com.neurelpress.blogs.service;


import com.neurelpress.blogs.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse getProfileByUsername(String username);

    UserResponse updateProfile(UUID userId, String displayName, String bio,
                              String avatarUrl, String githubUrl,
                              String linkedinUrl, String websiteUrl);

    long getPublishedArticleCount(UUID userId);
}
