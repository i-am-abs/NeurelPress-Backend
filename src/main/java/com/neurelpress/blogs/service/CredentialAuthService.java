package com.neurelpress.blogs.service;

import com.neurelpress.blogs.dto.request.LoginRequest;
import com.neurelpress.blogs.dto.request.RegisterRequest;
import com.neurelpress.blogs.dto.response.AuthResponse;

public interface CredentialAuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
