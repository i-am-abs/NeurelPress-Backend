package com.neurelpress.blogs.service.impl;

import com.neurelpress.blogs.constants.CodeConstants;
import com.neurelpress.blogs.constants.AuthProvider;
import com.neurelpress.blogs.dao.RefreshToken;
import com.neurelpress.blogs.dao.User;
import com.neurelpress.blogs.dto.response.AuthResponse;
import com.neurelpress.blogs.dto.response.OAuthTokenPair;
import com.neurelpress.blogs.mapper.UserMapper;
import com.neurelpress.blogs.repository.RefreshTokenRepository;
import com.neurelpress.blogs.repository.UserRepository;
import com.neurelpress.blogs.security.jwt.JwtTokenProvider;
import com.neurelpress.blogs.service.UserService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserMapper userMapper;

    public void recordSignIn(@NonNull User user, AuthProvider via) {
        user.setLastSignInAt(Instant.now());
        user.setLastSignInVia(via);
        userRepository.save(user);
    }

    public @NonNull OAuthTokenPair issueOAuthTokenPair(@NonNull User user) {
        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = persistRefreshToken(user);
        return new OAuthTokenPair(accessToken, refreshToken);
    }

    public @NonNull AuthResponse issueAuthResponse(@NonNull User user) {
        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = persistRefreshToken(user);
        long publishedCount = userService.getPublishedArticleCount(user.getId());
        return new AuthResponse(
                accessToken,
                refreshToken,
                CodeConstants.BEARER,
                tokenProvider.getAccessExpirationMs() / 1000,
                userMapper.toResponse(user, publishedCount)
        );
    }

    private String persistRefreshToken(User user) {
        String refreshTokenStr = tokenProvider.generateRefreshToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiresAt(Instant.now().plusMillis(tokenProvider.getRefreshExpirationMs()))
                .build();
        refreshTokenRepository.save(refreshToken);
        return refreshTokenStr;
    }
}

