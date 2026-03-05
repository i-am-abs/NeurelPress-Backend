package com.neurelpress.blogs.controller;

import com.neurelpress.blogs.constants.ApiConstants;
import com.neurelpress.blogs.constants.CodeConstants;
import com.neurelpress.blogs.dto.request.*;
import com.neurelpress.blogs.dto.response.AuthResponse;
import com.neurelpress.blogs.dto.response.UserResponse;
import com.neurelpress.blogs.security.UserPrincipal;
import com.neurelpress.blogs.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping(ApiConstants.Api_Auth)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping(ApiConstants.Register)
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registering user: {}", request);
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping(ApiConstants.Login)
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Logging in user: {}", request);
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping(ApiConstants.Refresh)
    @Operation(summary = "Refresh access token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refreshing token: {}", request);
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping(ApiConstants.Logout)
    @Operation(summary = "Logout and revoke tokens")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserPrincipal principal) {
        authService.logout(principal.getId());
        log.info("User logged out: {}", principal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping(ApiConstants.Me)
    @Operation(summary = "Get current authenticated user")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new com.neurelpress.blogs.exception.UnauthorizedException("Unauthorized");
        }
        log.info("Getting current user: {}", principal.getId());
        return ResponseEntity.ok(authService.getCurrentUser(principal.getId()));
    }

    @GetMapping(ApiConstants.Verify_Email)
    @Operation(summary = "Verify email with token (from link in email)")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        try {
            authService.verifyEmail(token);
            log.info("Email verified: {}", token);
            return ResponseEntity.ok(Map.of(CodeConstants.MESSAGE, CodeConstants.EMAIL_VERIFICATION_SUCCESSFUL));
        } catch (Exception e) {
            log.error("Email verification failed: {}", token, e);
            return ResponseEntity.badRequest().body(Map.of(CodeConstants.MESSAGE, CodeConstants.EMAIL_VERIFICATION_FAILED));
        }
    }

    @PostMapping(ApiConstants.Resend_Verification_Email)
    @Operation(summary = "Resend verification email")
    public ResponseEntity<Map<String, String>> resendVerification(@AuthenticationPrincipal UserPrincipal principal) {
        authService.resendVerificationEmail(principal.getId());
        log.info("Verification email resent: {}", principal.getId());
        return ResponseEntity.ok(Map.of(CodeConstants.MESSAGE, CodeConstants.EMAIL_VERIFICATION_SENT));
    }

    @PostMapping(ApiConstants.Request_Otp)
    @Operation(summary = "Request a login OTP to be sent to email")
    public ResponseEntity<Map<String, String>> requestOtp(@Valid @RequestBody OtpRequest request) {
        authService.requestLoginOtp(request);
        log.info("Requested OTP For Login");
        return ResponseEntity.ok(Map.of(CodeConstants.MESSAGE, "OTP sent if email exists"));
    }

    @PostMapping(ApiConstants.Login_Otp)
    @Operation(summary = "Login with email and OTP")
    public ResponseEntity<AuthResponse> loginWithOtp(@Valid @RequestBody OtpLoginRequest request) {
        log.info("Logged with OTP: {}", request);
        return ResponseEntity.ok(authService.loginWithOtp(request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset email")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request.email());
        return ResponseEntity.ok(Map.of(CodeConstants.MESSAGE, CodeConstants.PASSWORD_RESET_EMAIL_SENT));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with token from email")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(Map.of(CodeConstants.MESSAGE, CodeConstants.PASSWORD_RESET_SUCCESSFUL));
    }
}
