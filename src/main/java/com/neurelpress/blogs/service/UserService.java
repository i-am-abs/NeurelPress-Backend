package com.neurelpress.blogs.service;


import com.neurelpress.blogs.dto.response.PageResponse;
import com.neurelpress.blogs.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse getProfileByUsername(String username);

    UserResponse updateProfile(UUID userId, String displayName, String headline, String bio,
                               String avatarUrl, String githubUrl,
                               String linkedinUrl, String websiteUrl, String techTags);

    long getPublishedArticleCount(UUID userId);

    PageResponse<UserResponse> searchUsers(String query, int page, int size);
}
