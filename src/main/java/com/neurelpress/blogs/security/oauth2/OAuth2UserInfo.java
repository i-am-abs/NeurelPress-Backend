package com.neurelpress.blogs.security.oauth2;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public record OAuth2UserInfo(
        String id,
        String name,
        String email,
        String avatarUrl
) {
    @Contract("_ -> new")
    public static @NonNull OAuth2UserInfo fromGoogle(@NonNull Map<String, Object> attributes) {
        return new OAuth2UserInfo(
                (String) attributes.get("sub"),
                (String) attributes.get("name"),
                (String) attributes.get("email"),
                (String) attributes.get("picture")
        );
    }

    @Contract("_ -> new")
    public static @NonNull OAuth2UserInfo fromGithub(@NonNull Map<String, Object> attributes) {
        return new OAuth2UserInfo(
                String.valueOf(attributes.get("id")),
                (String) attributes.get("name"),
                (String) attributes.get("email"),
                (String) attributes.get("avatar_url")
        );
    }
}
