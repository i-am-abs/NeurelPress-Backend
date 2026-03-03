package com.neurelpress.blogs.service.impl;

import com.neurelpress.blogs.constants.CodeConstants;
import com.neurelpress.blogs.dto.request.LoginRequest;
import com.neurelpress.blogs.dto.request.RefreshTokenRequest;
import com.neurelpress.blogs.dto.request.RegisterRequest;
import com.neurelpress.blogs.dto.response.AuthResponse;
import com.neurelpress.blogs.dto.response.UserResponse;
import com.neurelpress.blogs.dao.EmailVerificationToken;
import com.neurelpress.blogs.dao.RefreshToken;
import com.neurelpress.blogs.dao.User;
import com.neurelpress.blogs.constants.enums.AuthProvider;
import com.neurelpress.blogs.exception.DuplicateResourceException;
import com.neurelpress.blogs.exception.ResourceNotFoundException;
import com.neurelpress.blogs.exception.UnauthorizedException;
import com.neurelpress.blogs.mapper.UserMapper;
import com.neurelpress.blogs.repository.EmailVerificationTokenRepository;
import com.neurelpress.blogs.repository.RefreshTokenRepository;
import com.neurelpress.blogs.repository.UserRepository;
import com.neurelpress.blogs.security.jwt.JwtTokenProvider;
import com.neurelpress.blogs.service.AuthService;
import com.neurelpress.blogs.service.EmailService;
import com.neurelpress.blogs.utils.SecureTokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already registered");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already taken");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(request.displayName() != null ? request.displayName() : request.username())
                .provider(AuthProvider.LOCAL)
                .verified(false)
                .build();

        user = userRepository.save(user);

        String token = SecureTokenGenerator.generateHexToken();
        EmailVerificationToken evToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(Instant.now().plusSeconds(CodeConstants.EMAIL_VERIFICATION_EXPIRE_HOURS * 3600L))
                .build();
        emailVerificationTokenRepository.save(evToken);
        emailService.sendVerificationEmail(user.getEmail(), user.getDisplayName(), token);

        log.info("User registered: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if(user.getProvider() != AuthProvider.LOCAL) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!user.isVerified()) {
            throw new UnauthorizedException("Email not verified");
        }

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenAndRevokedFalse(request.refreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new UnauthorizedException("Refresh token expired");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();

        log.info("User logged & refreshed Token with: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(CodeConstants.USER, CodeConstants.ID, userId));
        log.info("User fetched: {}", user.getEmail());
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken evToken = emailVerificationTokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired verification link"));

        if (evToken.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Verification link has expired");
        }

        User user = evToken.getUser();
        user.setVerified(true);
        userRepository.save(user);
        evToken.setUsed(true);
        emailVerificationTokenRepository.save(evToken);
        emailVerificationTokenRepository.deleteByUserIdOrExpired(user.getId(), Instant.now());
        log.info("Email verified: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void resendVerificationEmail(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(CodeConstants.USER, CodeConstants.SLUG, userId));
        if (user.isVerified()) {
            throw new DuplicateResourceException("Email is already verified");
        }
        if (user.getProvider() != AuthProvider.LOCAL) {
            return;
        }

        emailVerificationTokenRepository.deleteByUserIdOrExpired(userId, Instant.now());
        String token = SecureTokenGenerator.generateHexToken();
        EmailVerificationToken evToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(Instant.now().plusSeconds(CodeConstants.EMAIL_VERIFICATION_EXPIRE_HOURS * 3600L))
                .build();
        emailVerificationTokenRepository.save(evToken);
        emailService.sendVerificationEmail(user.getEmail(), user.getDisplayName(), token);
        log.info("Verification email resent: {}", user.getEmail());
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshTokenStr = tokenProvider.generateRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiresAt(Instant.now().plusMillis(tokenProvider.getRefreshExpirationMs()))
                .build();
        refreshTokenRepository.save(refreshToken);
        log.info("New refresh token created: {}", refreshTokenStr);
        return new AuthResponse(accessToken, refreshTokenStr, CodeConstants.BEARER,
                tokenProvider.getAccessExpirationMs() / 1000, userMapper.toResponse(user));
    }
}
