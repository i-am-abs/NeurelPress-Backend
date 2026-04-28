package com.neurelpress.blogs.service.impl;

import com.neurelpress.blogs.config.GoogleIdTokenVerifier;
import com.neurelpress.blogs.constants.CodeConstants;
import com.neurelpress.blogs.constants.AuthProvider;
import com.neurelpress.blogs.dao.User;
import com.neurelpress.blogs.dto.response.AuthResponse;
import com.neurelpress.blogs.dto.response.OAuthTokenPair;
import com.neurelpress.blogs.exception.UnauthorizedException;
import com.neurelpress.blogs.repository.UserRepository;
import com.neurelpress.blogs.service.OAuthAuthService;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuthAuthServiceImpl implements OAuthAuthService {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_NAME = "name";
    private static final String CLAIM_PICTURE = "picture";

    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final UserRepository userRepository;
    private final AuthTokenService authTokenService;

    @Override
    @Transactional
    public AuthResponse signInWithGoogleIdToken(String idToken) {
        JWTClaimsSet claims = googleIdTokenVerifier.verify(idToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid Google ID token"));

        GoogleClaims g = readClaims(claims);
        if (g.email() == null || g.email().isBlank()) {
            throw new UnauthorizedException("Google account did not return an email");
        }

        User user = upsertGoogleUser(g);
        authTokenService.recordSignIn(user, AuthProvider.GOOGLE);
        return authTokenService.issueAuthResponse(user);
    }

    @Override
    @Transactional
    public OAuthTokenPair finalizeOAuthLogin(User user, AuthProvider signInVia) {
        authTokenService.recordSignIn(user, signInVia);
        OAuthTokenPair tokenPair = authTokenService.issueOAuthTokenPair(user);
        log.info("OAuth token pair issued: user={}, via={}", user.getEmail(), signInVia);
        return tokenPair;
    }

    private @NonNull User upsertGoogleUser(@NonNull GoogleClaims g) {
        Optional<User> linked = userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, g.subject());
        if (linked.isPresent()) {
            return applyProfileUpdates(linked.get(), g);
        }
        Optional<User> byEmail = userRepository.findByEmail(g.email());
        if (byEmail.isPresent()) {
            User existing = byEmail.get();
            if (existing.getProvider() != AuthProvider.GOOGLE && existing.getProvider() != null) {
                throw new UnauthorizedException("Email is registered with a different provider");
            }
            existing.setProviderId(g.subject());
            existing.setProvider(AuthProvider.GOOGLE);
            return applyProfileUpdates(userRepository.save(existing), g);
        }
        return userRepository.save(buildNewUser(g));
    }

    private @NonNull User buildNewUser(@NonNull GoogleClaims g) {
        String baseUsername = g.name() != null
                ? g.name().toLowerCase().replaceAll("[^a-z0-9]", "")
                : CodeConstants.USER;
        String username = baseUsername + "-" + UUID.randomUUID().toString().substring(0, 6);
        return User.builder()
                .username(username)
                .email(g.email())
                .displayName(g.name())
                .avatarUrl(g.picture())
                .provider(AuthProvider.GOOGLE)
                .providerId(g.subject())
                .verified(true)
                .build();
    }

    private User applyProfileUpdates(User user, @NonNull GoogleClaims g) {
        boolean dirty = false;
        if (g.name() != null && !g.name().isBlank()) {
            user.setDisplayName(g.name());
            dirty = true;
        }
        if (g.picture() != null && !g.picture().isBlank()) {
            user.setAvatarUrl(g.picture());
            dirty = true;
        }
        return dirty ? userRepository.save(user) : user;
    }

    @Contract("_ -> new")
    private @NonNull GoogleClaims readClaims(@NonNull JWTClaimsSet claims) {
        try {
            return new GoogleClaims(
                    claims.getSubject(),
                    claims.getStringClaim(CLAIM_EMAIL),
                    claims.getStringClaim(CLAIM_NAME),
                    claims.getStringClaim(CLAIM_PICTURE)
            );
        } catch (ParseException e) {
            throw new UnauthorizedException("Malformed Google ID token claims");
        }
    }

    private record GoogleClaims(String subject, String email, String name, String picture) {}
}
