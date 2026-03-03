package com.neurelpress.blogs.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OtpLoginRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 6, max = 6)
        @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be a 6 digit code")
        String otp
) {
}
