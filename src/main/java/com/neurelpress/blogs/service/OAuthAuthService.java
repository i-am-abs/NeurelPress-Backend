package com.neurelpress.blogs.service;

import com.neurelpress.blogs.constants.AuthProvider;
import com.neurelpress.blogs.dao.User;
import com.neurelpress.blogs.dto.response.AuthResponse;
import com.neurelpress.blogs.dto.response.OAuthTokenPair;

public interface OAuthAuthService {
    AuthResponse signInWithGoogleIdToken(String idToken);

    OAuthTokenPair finalizeOAuthLogin(User user, AuthProvider signInVia);
}
