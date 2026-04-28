package com.neurelpress.blogs.security.oauth2;

import com.neurelpress.blogs.dto.properties.NeuralPressCorsProperties;
import com.neurelpress.blogs.constants.ApiConstants;
import com.neurelpress.blogs.constants.CodeConstants;
import com.neurelpress.blogs.constants.AuthProvider;
import com.neurelpress.blogs.dao.User;
import com.neurelpress.blogs.dto.response.OAuthTokenPair;
import com.neurelpress.blogs.repository.UserRepository;
import com.neurelpress.blogs.service.OAuthAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuthAuthService oAuthAuthService;
    private final UserRepository userRepository;
    private final NeuralPressCorsProperties corsProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        boolean isGoogle = AuthProvider.GOOGLE.getAuthProvider().equals(registrationId);
        OAuth2UserInfo userInfo = isGoogle
                ? OAuth2UserInfo.fromGoogle(attributes)
                : OAuth2UserInfo.fromGithub(attributes);

        AuthProvider provider = isGoogle ? AuthProvider.GOOGLE : AuthProvider.GITHUB;

        if (userInfo.email() == null || userInfo.email().isBlank()) {
            getRedirectStrategy().sendRedirect(request, response, errorRedirect("oauth_email_required"));
            log.warn("OAuth login rejected: missing email (provider={})", registrationId);
            return;
        }

        Optional<User> resolved = resolveOAuthUser(request, response, userInfo, provider);
        if (resolved.isEmpty()) {
            return;
        }
        User user = resolved.get();

        applyOAuthProfile(user, userInfo);
        OAuthTokenPair tokens = oAuthAuthService.finalizeOAuthLogin(user, provider);

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendBase() + ApiConstants.Auth_Callback)
                .queryParam(CodeConstants.TOKEN, tokens.accessToken())
                .queryParam(CodeConstants.REFRESH_TOKEN, tokens.refreshToken())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        log.info("OAuth login success: {} via {}", user.getEmail(), provider);
    }

    private @NonNull String frontendBase() {
        return corsProperties.app().primaryFrontendUrl();
    }

    private @NonNull String errorRedirect(String code) {
        return frontendBase() + "/login?error=" + code;
    }

    private Optional<User> resolveOAuthUser(HttpServletRequest request,
                                            HttpServletResponse response,
                                            @NonNull OAuth2UserInfo userInfo,
                                            AuthProvider provider) throws IOException {
        Optional<User> linked = userRepository.findByProviderAndProviderId(provider, userInfo.id());
        if (linked.isPresent()) {
            return linked;
        }

        Optional<User> byEmail = userRepository.findByEmail(userInfo.email());
        if (byEmail.isPresent()) {
            User existing = byEmail.get();
            if (existing.getProvider() == AuthProvider.LOCAL) {
                log.info("OAuth blocked: email {} already uses password login", userInfo.email());
                getRedirectStrategy().sendRedirect(request, response, errorRedirect("oauth_email_registered"));
                return Optional.empty();
            }
            if (existing.getProvider() != provider) {
                log.info("OAuth blocked: email {} registered with {}", userInfo.email(), existing.getProvider());
                getRedirectStrategy().sendRedirect(request, response, errorRedirect("oauth_provider_mismatch"));
                return Optional.empty();
            }
            existing.setProviderId(userInfo.id());
            return Optional.of(userRepository.save(existing));
        }

        return Optional.of(createOAuthUser(userInfo, provider));
    }

    private @NonNull User createOAuthUser(@NonNull OAuth2UserInfo info, AuthProvider provider) {
        String baseUsername = info.name() != null
                ? info.name().toLowerCase().replaceAll("[^a-z0-9]", "") : CodeConstants.USER;
        String username = baseUsername + "-" + UUID.randomUUID().toString().substring(0, 6);

        User user = User.builder()
                .username(username)
                .email(info.email())
                .displayName(info.name())
                .avatarUrl(info.avatarUrl())
                .provider(provider)
                .providerId(info.id())
                .verified(true)
                .build();
        log.info("Created new OAuth user: {}", user.getEmail());
        return userRepository.save(user);
    }

    private void applyOAuthProfile(User user, @NonNull OAuth2UserInfo info) {
        boolean dirty = false;
        if (info.name() != null && !info.name().isBlank()) {
            user.setDisplayName(info.name());
            dirty = true;
        }
        if (info.avatarUrl() != null && !info.avatarUrl().isBlank()) {
            user.setAvatarUrl(info.avatarUrl());
            dirty = true;
        }
        if (dirty) {
            userRepository.save(user);
        }
    }
}
