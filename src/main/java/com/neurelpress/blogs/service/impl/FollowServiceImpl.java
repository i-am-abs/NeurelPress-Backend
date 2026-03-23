package com.neurelpress.blogs.service.impl;

import com.neurelpress.blogs.constants.CodeConstants;
import com.neurelpress.blogs.dao.Follow;
import com.neurelpress.blogs.dao.User;
import com.neurelpress.blogs.dto.response.PageResponse;
import com.neurelpress.blogs.dto.response.UserResponse;
import com.neurelpress.blogs.exception.ResourceNotFoundException;
import com.neurelpress.blogs.mapper.UserMapper;
import com.neurelpress.blogs.repository.ArticleRepository;
import com.neurelpress.blogs.repository.FollowRepository;
import com.neurelpress.blogs.repository.UserRepository;
import com.neurelpress.blogs.service.FollowService;
import com.neurelpress.blogs.utils.PageResponseSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ArticleRepository articleRepository;

    @Override
    @Transactional
    public void toggleFollow(@NonNull UUID followerId, UUID followingId) {
        if (followerId.equals(followingId)) {
            return;
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException(CodeConstants.USER, CodeConstants.ID, followerId));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new ResourceNotFoundException(CodeConstants.USER, CodeConstants.ID, followingId));

        followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .ifPresentOrElse(
                        followRepository::delete,
                        () -> followRepository.save(Follow.builder().follower(follower).following(following).build())
                );

        log.info("Follow toggled: {} by {}", followingId, followerId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(@NonNull UUID followerId, UUID followingId) {
        if (followerId.equals(followingId)) {
            return false;
        }
        log.info("Checking if user: {} is following user: {}", followerId, followingId);
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getFollowerCount(UUID userId) {
        return followRepository.countByFollowingId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getFollowingCount(UUID userId) {
        return followRepository.countByFollowerId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getFollowers(UUID followingId, int page, int size) {
        Page<com.neurelpress.blogs.dao.Follow> p = followRepository.findByFollowingId(followingId, Pageable.ofSize(size).withPage(page));
        return PageResponseSupport.from(p, f -> {
            var u = f.getFollower();
            return userMapper.toResponse(u, articleRepository.countPublishedByAuthor(u.getId()));
        });
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getFollowing(UUID followerId, int page, int size) {
        Page<com.neurelpress.blogs.dao.Follow> p = followRepository.findByFollowerId(followerId, Pageable.ofSize(size).withPage(page));
        return PageResponseSupport.from(p, f -> {
            var u = f.getFollowing();
            return userMapper.toResponse(u, articleRepository.countPublishedByAuthor(u.getId()));
        });
    }
}
