package com.neurelpress.blogs.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleSignInRequest(

        @NotBlank(message = "Google ID token must not be blank")
        String idToken
) {}
