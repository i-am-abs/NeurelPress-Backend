package com.neurelpress.blogs.service.impl;

import com.neurelpress.blogs.constants.CodeConstants;
import com.neurelpress.blogs.constants.enums.AuthProvider;
import com.neurelpress.blogs.dao.User;
import com.neurelpress.blogs.dao.EmailVerificationToken;
import com.neurelpress.blogs.dao.PasswordResetToken;
import com.neurelpress.blogs.dao.RefreshToken;
import com.neurelpress.blogs.dao.EmailOtp;
import com.neurelpress.blogs.dto.request.LoginRequest;
import com.neurelpress.blogs.dto.request.OtpLoginRequest;
import com.neurelpress.blogs.dto.request.RefreshTokenRequest;
import com.neurelpress.blogs.dto.request.RegisterRequest;
import com.neurelpress.blogs.dto.request.OtpRequest;
import com.neurelpress.blogs.dto.response.AuthResponse;
import com.neurelpress.blogs.dto.response.OAuthTokenPair;
import com.neurelpress.blogs.dto.response.UserResponse;
import com.neurelpress.blogs.exception.DuplicateResourceException;
import com.neurelpress.blogs.exception.ResourceNotFoundException;
import com.neurelpress.blogs.exception.UnauthorizedException;
import com.neurelpress.blogs.mapper.UserMapper;
import com.neurelpress.blogs.repository.EmailVerificationTokenRepository;
import com.neurelpress.blogs.repository.RefreshTokenRepository;
import com.neurelpress.blogs.repository.UserRepository;
import com.neurelpress.blogs.repository.PasswordResetTokenRepository;
import com.neurelpress.blogs.repository.EmailOtpRepository;
import com.neurelpress.blogs.security.jwt.JwtTokenProvider;
import com.neurelpress.blogs.service.AuthService;
import com.neurelpress.blogs.service.EmailService;
import com.neurelpress.blogs.service.UserService;
import com.neurelpress.blogs.utils.SecureTokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailOtpRepository emailOtpRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponse register(@NonNull RegisterRequest request) {
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
        recordSignIn(user, AuthProvider.LOCAL);

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
    public AuthResponse login(@NonNull LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!user.isVerified()) {
            throw new UnauthorizedException("Email not verified");
        }

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        recordSignIn(user, AuthProvider.LOCAL);
        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(@NonNull RefreshTokenRequest request) {
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
        long publishedCount = userService.getPublishedArticleCount(user.getId());
        return userMapper.toResponse(user, publishedCount);
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

    @Override
    @Transactional
    public void requestLoginOtp(@NonNull OtpRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException(CodeConstants.USER, CodeConstants.Email, request.email()));

        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new UnauthorizedException("OTP login is only available for email/password accounts");
        }

        emailOtpRepository.deleteByUserIdOrExpired(user.getId(), Instant.now());

        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        EmailOtp otp = EmailOtp.builder()
                .user(user)
                .code(code)
                .expiresAt(Instant.now().plusSeconds(600))
                .build();
        emailOtpRepository.save(otp);
        emailService.sendLoginOtpEmail(user.getEmail(), user.getDisplayName(), code);
    }

    @Override
    @Transactional
    public AuthResponse loginWithOtp(@NonNull OtpLoginRequest request) {
        EmailOtp otp = emailOtpRepository.findActiveByEmailAndCode(request.email(), request.otp())
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired OTP"));

        if (otp.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Invalid or expired OTP");
        }

        User user = otp.getUser();
        user.setVerified(true);
        userRepository.save(user);

        otp.setUsed(true);
        emailOtpRepository.save(otp);
        emailOtpRepository.deleteByUserIdOrExpired(user.getId(), Instant.now());

        recordSignIn(user, AuthProvider.LOCAL);
        log.info("User logged in with OTP: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public OAuthTokenPair finalizeOAuthLogin(User user, AuthProvider signInVia) {
        recordSignIn(user, signInVia);
        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshTokenStr = tokenProvider.generateRefreshToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiresAt(Instant.now().plusMillis(tokenProvider.getRefreshExpirationMs()))
                .build();
        refreshTokenRepository.save(refreshToken);
        return new OAuthTokenPair(accessToken, refreshTokenStr);
    }

    private void recordSignIn(@NonNull User user, AuthProvider via) {
        user.setLastSignInAt(Instant.now());
        user.setLastSignInVia(via);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getProvider() != AuthProvider.LOCAL) {
                return;
            }
            passwordResetTokenRepository.deleteByUserIdOrExpired(user.getId(), Instant.now());
            String token = SecureTokenGenerator.generateHexToken();
            PasswordResetToken prToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiresAt(Instant.now().plusSeconds(CodeConstants.PASSWORD_RESET_EXPIRE_HOURS * 3600L))
                    .build();
            passwordResetTokenRepository.save(prToken);
            emailService.sendPasswordResetEmail(user.getEmail(), user.getDisplayName(), token);
            log.info("Password reset email sent to {}", email);
        });
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prToken = passwordResetTokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired reset link"));
        if (prToken.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Reset link has expired");
        }
        User user = prToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        prToken.setUsed(true);
        passwordResetTokenRepository.save(prToken);
        passwordResetTokenRepository.deleteByUserIdOrExpired(user.getId(), Instant.now());
        log.info("Password reset completed for user: {}", user.getEmail());
    }

    @Contract("_ -> new")
    private @NonNull AuthResponse buildAuthResponse(@NonNull User user) {
        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshTokenStr = tokenProvider.generateRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiresAt(Instant.now().plusMillis(tokenProvider.getRefreshExpirationMs()))
                .build();
        refreshTokenRepository.save(refreshToken);
        log.info("New refresh token created: {}", refreshTokenStr);
        long publishedCount = userService.getPublishedArticleCount(user.getId());
        return new AuthResponse(accessToken, refreshTokenStr, CodeConstants.BEARER,
                tokenProvider.getAccessExpirationMs() / 1000, userMapper.toResponse(user, publishedCount));
    }
}
