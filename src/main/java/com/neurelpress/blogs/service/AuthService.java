package com.neurelpress.blogs.service;

import com.neurelpress.blogs.dto.request.*;
import com.neurelpress.blogs.dto.response.AuthResponse;
import com.neurelpress.blogs.dto.response.UserResponse;

import java.util.UUID;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(UUID userId);

    UserResponse getCurrentUser(UUID userId);

    void verifyEmail(String token);

    void resendVerificationEmail(UUID userId);

    void requestLoginOtp(OtpRequest request);

    AuthResponse loginWithOtp(OtpLoginRequest request);

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);
}
