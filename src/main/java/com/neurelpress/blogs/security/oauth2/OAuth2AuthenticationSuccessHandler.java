package com.neurelpress.blogs.security.oauth2;

import com.neurelpress.blogs.constants.ApiConstants;
import com.neurelpress.blogs.constants.CodeConstants;
import com.neurelpress.blogs.constants.enums.AuthProvider;
import com.neurelpress.blogs.dao.User;
import com.neurelpress.blogs.repository.UserRepository;
import com.neurelpress.blogs.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Value("${neuralpress.cors.allowed-origins}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        OAuth2UserInfo userInfo = AuthProvider.GOOGLE.getAuthProvider().equals(registrationId)
                ? OAuth2UserInfo.fromGoogle(attributes)
                : OAuth2UserInfo.fromGithub(attributes);

        AuthProvider provider = AuthProvider.GOOGLE.getAuthProvider().equals(registrationId) ? AuthProvider.GOOGLE : AuthProvider.GITHUB;

        User user = userRepository.findByProviderAndProviderId(provider, userInfo.id())
                .orElseGet(() -> createOAuthUser(userInfo, provider));

        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + ApiConstants.Auth_Callback)
                .queryParam(CodeConstants.TOKEN, accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        log.info("User logged in successfully: {}", user.getEmail());
    }

    private User createOAuthUser(OAuth2UserInfo info, AuthProvider provider) {
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
        log.info("Created new user: {}", user.getEmail());
        return userRepository.save(user);
    }
}
