package com.neurelpress.blogs.dao;

import com.neurelpress.blogs.constants.AuthProvider;
import com.neurelpress.blogs.constants.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "users")
@CompoundIndex(name = "idx_user_provider_provider_id", def = "{'provider': 1, 'providerId': 1}")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "passwordHash")
public class User {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    @Builder.Default
    private AuthProvider provider = AuthProvider.LOCAL;

    private String providerId;

    private String bio;

    private String headline;
    private String avatarUrl;
    private String displayName;

    private String techTags;

    @Builder.Default
    private UserRole role = UserRole.USER;

    @Builder.Default
    private boolean verified = false;

    @Builder.Default
    private int followersCount = 0;

    @Builder.Default
    private int followingCount = 0;

    private String githubUrl;
    private String linkedinUrl;
    private String websiteUrl;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private Instant lastSignInAt;

    private AuthProvider lastSignInVia;
}
