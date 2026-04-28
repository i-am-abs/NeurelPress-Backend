package com.neurelpress.blogs.service;

import com.neurelpress.blogs.constants.enums.AuthProvider;
import com.neurelpress.blogs.dao.User;
import com.neurelpress.blogs.dto.response.AuthResponse;
import com.neurelpress.blogs.dto.response.OAuthTokenPair;

public interface OAuthAuthService {
    AuthResponse googleSignIn(String idToken);
    OAuthTokenPair finalizeOAuthLogin(User user, AuthProvider signInVia);
}
