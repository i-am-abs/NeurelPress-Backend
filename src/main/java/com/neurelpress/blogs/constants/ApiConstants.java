package com.neurelpress.blogs.constants;

public final class ApiConstants {
    public static final String Api_Ai = "/api/ai";
    public static final String Api_Articles = "/api/articles";
    public static final String Api_Auth = "/api/auth";
    public static final String Api_Books = "/api/books";
    public static final String Api_Bookmarks = "/api/bookmarks";
    public static final String Api_Feed = "/api/feed";
    public static final String Api_Quotes = "/api/quotes";
    public static final String Api_Tags = "/api/tags";
    public static final String Api_Trending = "/api/trending";
    public static final String Api_Users = "/api/users";

    public static final String Me_Drafts = "/me/drafts";
    public static final String Me_Profile = "/me/profile";

    public static final String Auth_Callback = "/auth/callback";

    public static final String Suggested_Tags ="/suggest-tags";
    public static final String Suggested_Title = "/suggest-title";
    public static final String Suggested_Summary = "/suggest-summary";

    public static final String Register = "/register";
    public static final String Login = "/login";
    public static final String Refresh = "/refresh";
    public static final String Logout = "/logout";
    public static final String Me = "/me";

    public static final String Latest = "/latest";
    public static final String Top = "/top";
    public static final String Verify_Email = "/verify-email";
    public static final String Resend_Verification_Email = "/resend-verification-email";

    public static final String Search = "/search";
    public static final String Really_Simple_Syndication = "/rss";
    public static final String Today = "/today";

    private ApiConstants() {
        throw new UnsupportedOperationException("Api Constant class cannot be instantiated");
    }
}
