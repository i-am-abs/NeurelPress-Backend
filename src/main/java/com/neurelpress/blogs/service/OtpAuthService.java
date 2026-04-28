package com.neurelpress.blogs.service;

import com.neurelpress.blogs.dto.request.OtpLoginRequest;
import com.neurelpress.blogs.dto.request.OtpRequest;
import com.neurelpress.blogs.dto.response.AuthResponse;

public interface OtpAuthService {

    void requestOtp(OtpRequest request);

    AuthResponse loginWithOtp(OtpLoginRequest request);
}
