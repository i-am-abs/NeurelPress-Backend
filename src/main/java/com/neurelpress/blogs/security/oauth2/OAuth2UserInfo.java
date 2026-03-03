package com.neurelpress.blogs.security.oauth2;

import java.util.Map;

public record OAuth2UserInfo(
        String id,
        String name,
        String email,
        String avatarUrl
) {
    public static OAuth2UserInfo fromGoogle(Map<String, Object> attributes) {
        return new OAuth2UserInfo(
                (String) attributes.get("sub"),
                (String) attributes.get("name"),
                (String) attributes.get("email"),
                (String) attributes.get("picture")
        );
    }

    public static OAuth2UserInfo fromGithub(Map<String, Object> attributes) {
        return new OAuth2UserInfo(
                String.valueOf(attributes.get("id")),
                (String) attributes.get("name"),
                (String) attributes.get("email"),
                (String) attributes.get("avatar_url")
        );
    }
}
