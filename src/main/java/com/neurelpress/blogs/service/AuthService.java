package com.neurelpress.blogs.service;

import com.neurelpress.blogs.dto.request.LoginRequest;
import com.neurelpress.blogs.dto.request.OtpLoginRequest;
import com.neurelpress.blogs.dto.request.OtpRequest;
import com.neurelpress.blogs.dto.request.RefreshTokenRequest;
import com.neurelpress.blogs.dto.request.RegisterRequest;
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
}
