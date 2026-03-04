package com.neurelpress.blogs.controller;

import com.neurelpress.blogs.constants.ApiConstants;
import com.neurelpress.blogs.constants.CodeConstants;
import com.neurelpress.blogs.dto.response.UserResponse;
import com.neurelpress.blogs.dto.response.PageResponse;
import com.neurelpress.blogs.security.UserPrincipal;
import com.neurelpress.blogs.service.FollowService;
import com.neurelpress.blogs.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.Api_Users)
@Tag(name = "Users", description = "User profile and follow endpoints")
public class UserController {

    private final UserService userService;
    private final FollowService followService;

    @GetMapping("/{username}/profile")
    @Operation(summary = "Get User Profile by Username")
    public ResponseEntity<UserResponse> getProfile(@PathVariable String username) {
        log.info("Getting profile for user: {}", username);
        return ResponseEntity.ok(userService.getProfileByUsername(username));
    }

    @GetMapping(ApiConstants.Search)
    @Operation(summary = "Search users by username or display name")
    public ResponseEntity<PageResponse<UserResponse>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        log.info("Searching users with query: {}, page: {}, size: {}", query, page, size);
        return ResponseEntity.ok(userService.searchUsers(query, page, size));
    }

    @PatchMapping(ApiConstants.Me_Profile)
    @Operation(summary = "Update current User's profile")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody Map<String, String> updateDetails) {
        log.info("Updating profile for user: {}", userPrincipal.getId());
        return ResponseEntity.ok(userService.updateProfile(
                userPrincipal.getId(),
                updateDetails.get("displayName"),
                updateDetails.get("headline"),
                updateDetails.get("bio"),
                updateDetails.get("avatarUrl"),
                updateDetails.get("githubUrl"),
                updateDetails.get("linkedinUrl"),
                updateDetails.get("websiteUrl"),
                updateDetails.get("techTags")
        ));
    }

    @PostMapping("/{userId}/follow")
    @Operation(summary = "Toggle follow/unfollow a user")
    public ResponseEntity<Void> toggleFollow(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID userId) {
        followService.toggleFollow(userPrincipal.getId(), userId);
        log.info("Follow toggled for user: {} on user: {}", userPrincipal.getId(), userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/following")
    @Operation(summary = "Check if current user follows another user")
    public ResponseEntity<Map<String, Boolean>> isFollowing(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID userId) {
        log.info("Checking if user: {} is following user: {}", userPrincipal.getId(), userId);
        return ResponseEntity.ok(Map.of(CodeConstants.FOLLOWING, followService.isFollowing(
                userPrincipal.getId(), userId
        )));
    }
}
