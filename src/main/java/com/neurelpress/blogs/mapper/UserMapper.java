package com.neurelpress.blogs.mapper;

import com.neurelpress.blogs.dao.User;
import com.neurelpress.blogs.dto.response.ArticleResponse;
import com.neurelpress.blogs.dto.response.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserMapper {

    public UserResponse toResponse(User user, long publishedArticleCount) {
        return toResponse(user, publishedArticleCount, null, null);
    }

    public UserResponse toResponse(User user, long publishedArticleCount, Long followersCount, Long followingCount) {
        log.debug("Mapping user: {}", user.getId());
        int followers = followersCount != null ? followersCount.intValue() : user.getFollowersCount();
        int following = followingCount != null ? followingCount.intValue() : user.getFollowingCount();
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getHeadline(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getRole().name(),
                user.getGithubUrl(),
                user.getLinkedinUrl(),
                user.getWebsiteUrl(),
                user.getTechTags(),
                user.isVerified(),
                followers,
                following,
                publishedArticleCount,
                user.getCreatedAt()
        );
    }

    public ArticleResponse.AuthorSummary toAuthorSummary(User user) {
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
