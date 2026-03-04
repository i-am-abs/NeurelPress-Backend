package com.neurelpress.blogs.mapper;

import com.neurelpress.blogs.dto.response.ArticleResponse;
import com.neurelpress.blogs.dto.response.UserResponse;
import com.neurelpress.blogs.dao.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserMapper {

    public UserResponse toResponse(User user, long publishedArticleCount) {
        log.info("Mapping user: {}", user);

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
                user.getFollowersCount(),
                user.getFollowingCount(),
                publishedArticleCount,
                user.getCreatedAt()
        );
    }

    public ArticleResponse.AuthorSummary toAuthorSummary(User user) {
        log.info("Mapping user for Author Summary: {}", user);

        return new ArticleResponse.AuthorSummary(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getBio()
        );
    }
}
