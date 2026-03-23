package com.neurelpress.blogs.mapper;

import com.neurelpress.blogs.constants.enums.AuthProvider;
import com.neurelpress.blogs.dao.User;
import com.neurelpress.blogs.dto.response.ArticleResponse;
import com.neurelpress.blogs.dto.response.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class UserMapper {

    public UserResponse toResponse(User user, long publishedArticleCount) {
        return toResponse(user, publishedArticleCount, null, null);
    }

    public UserResponse toResponse(@NonNull User user, long publishedArticleCount, Long followersCount, Long followingCount) {
        log.debug("Mapping user: {}", user.getId());
        int followers = followersCount != null ? followersCount.intValue() : user.getFollowersCount();
        int following = followingCount != null ? followingCount.intValue() : user.getFollowingCount();
        AuthProvider provider = user.getProvider() != null ? user.getProvider() : AuthProvider.LOCAL;
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getHeadline(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getRole().name(),
                provider.name(),
                user.getGithubUrl(),
                user.getLinkedinUrl(),
                user.getWebsiteUrl(),
                user.getTechTags(),
                user.isVerified(),
                followers,
                following,
                publishedArticleCount,
                allowedLoginMethods(provider),
                user.getCreatedAt(),
                user.getLastSignInAt()
        );
    }

    private static @NonNull @Unmodifiable List<String> allowedLoginMethods(@NonNull AuthProvider provider) {
        return switch (provider) {
            case LOCAL -> List.of("password", "otp");
            case GOOGLE -> List.of("google");
            case GITHUB -> List.of("github");
        };
    }

    public ArticleResponse.AuthorSummary toAuthorSummary(@NonNull User user) {
        log.debug("Mapping user for Author Summary: {}", user.getId());

        return new ArticleResponse.AuthorSummary(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getBio()
        );
    }
}
