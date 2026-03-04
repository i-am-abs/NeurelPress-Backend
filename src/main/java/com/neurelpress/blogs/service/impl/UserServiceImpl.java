package com.neurelpress.blogs.service.impl;

import com.neurelpress.blogs.constants.CodeConstants;
import com.neurelpress.blogs.dto.response.UserResponse;
import com.neurelpress.blogs.dto.response.PageResponse;
import com.neurelpress.blogs.dao.User;
import com.neurelpress.blogs.exception.ResourceNotFoundException;
import com.neurelpress.blogs.mapper.UserMapper;
import com.neurelpress.blogs.repository.ArticleRepository;
import com.neurelpress.blogs.repository.UserRepository;
import com.neurelpress.blogs.service.UserService;
import com.neurelpress.blogs.utils.PageResponseSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(CodeConstants.USER, CodeConstants.USERNAME, username));
        long publishedCount = articleRepository.countPublishedByAuthor(user.getId());
        return userMapper.toResponse(user, publishedCount);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UUID userId, String displayName, String headline, String bio,
                                      String avatarUrl, String githubUrl,
                                      String linkedinUrl, String websiteUrl, String techTags) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(CodeConstants.USER, CodeConstants.ID, userId));

        if (displayName != null) {
            user.setDisplayName(displayName);
        }
        if (headline != null) {
            user.setHeadline(headline);
        }
        if (bio != null) {
            user.setBio(bio);
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
        if (githubUrl != null) {
            user.setGithubUrl(githubUrl);
        }
        if (linkedinUrl != null) {
            user.setLinkedinUrl(linkedinUrl);
        }
        if (websiteUrl != null) {
            user.setWebsiteUrl(websiteUrl);
        }
        if (techTags != null) {
            user.setTechTags(techTags);
        }

        user = userRepository.save(user);
        long publishedCount = articleRepository.countPublishedByAuthor(user.getId());
        log.info("Updated user profile: {}", user.getUsername());
        return userMapper.toResponse(user, publishedCount);
    }

    @Override
    @Transactional(readOnly = true)
    public long getPublishedArticleCount(UUID userId) {
        log.info("Getting published article count for user: {}", userId);
        return articleRepository.countPublishedByAuthor(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> searchUsers(String query, int page, int size) {
        log.info("Searching users with query: {} (page {}, size {})", query, page, size);
        Page<User> p = userRepository.search(query, Pageable.ofSize(size).withPage(page));
        return PageResponseSupport.from(p, u -> userMapper.toResponse(u, 0L));
    }
}
