package com.neurelpress.blogs.service;

import com.neurelpress.blogs.dto.response.PageResponse;
import com.neurelpress.blogs.dto.response.UserResponse;

import java.util.UUID;

public interface FollowService {

    void toggleFollow(UUID followerId, UUID followingId);

    boolean isFollowing(UUID followerId, UUID followingId);

    long getFollowerCount(UUID userId);

    long getFollowingCount(UUID userId);

    PageResponse<UserResponse> getFollowers(UUID followingId, int page, int size);

    PageResponse<UserResponse> getFollowing(UUID followerId, int page, int size);
}
