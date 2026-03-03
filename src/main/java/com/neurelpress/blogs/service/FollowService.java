package com.neurelpress.blogs.service;

import java.util.UUID;

public interface FollowService {

    void toggleFollow(UUID followerId, UUID followingId);

    boolean isFollowing(UUID followerId, UUID followingId);
}
