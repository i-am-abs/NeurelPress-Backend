package com.neurelpress.blogs.service.impl;

import com.neurelpress.blogs.constants.CodeConstants;
import com.neurelpress.blogs.dao.Follow;
import com.neurelpress.blogs.dao.User;
import com.neurelpress.blogs.exception.ResourceNotFoundException;
import com.neurelpress.blogs.repository.FollowRepository;
import com.neurelpress.blogs.repository.UserRepository;
import com.neurelpress.blogs.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void toggleFollow(UUID followerId, UUID followingId) {
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
    public boolean isFollowing(UUID followerId, UUID followingId) {
        if (followerId.equals(followingId)) {
            return false;
        }
        log.info("Checking if user: {} is following user: {}", followerId, followingId);
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }
}
