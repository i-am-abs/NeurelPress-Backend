package com.neurelpress.blogs.constants;

public final class CodeConstants {
    public static final String BEARER = "Bearer";

    public static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta";

    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String SUMMARY = "summary";
    public static final String FOLLOWING = "following";
    public static final String TOKEN = "token";
    public static final String REFRESH_TOKEN = "refreshToken";
    public static final String MESSAGE = "message";
    public static final String USER = "user";
    public static final String USERNAME = "username";
    public static final String ID = "Id";
    public static final String BOOK = "Book";
    public static final String Article = "Article";
    public static final String SLUG = "slug";
    public static final String TAGS = "Tags";
    public static final String Email = "Email";

    public static final String EMAIL_VERIFICATION_SUCCESSFUL = "Email verified successfully";
    public static final String EMAIL_VERIFICATION_FAILED = "Email verification failed";
    public static final String EMAIL_VERIFICATION_SENT = "Verification email sent";
    public static final String PASSWORD_RESET_EMAIL_SENT = "If an account exists with this email, you will receive a reset link";
    public static final String PASSWORD_RESET_SUCCESSFUL = "Password has been reset successfully";

    public static final String TAG_SUGGESTION_PROMPT = """
            You are an expert at tagging technical and AI/ML articles for a scientific publishing platform.
            Given the following article title and a short excerpt, suggest exactly 3-5 relevant tags.
            Return ONLY a JSON array of tag names (lowercase, hyphenated for multi-word).
            Example: ["deep-learning","nlp","transformers"]
            Title: %s
            Excerpt: %s
            """;

    public static final String TITLE_SUGGESTION_PROMPT = """
            You are an editor for a technical blog.
            Suggest one concise, SEO-friendly title (max 80 chars).
            Return ONLY the title.
            Excerpt: %s
            """;

    public static final String SUMMARY_PROMPT = """
            Summarize this technical article in 1-2 sentences for a meta description (max 160 chars).
            Return ONLY the summary.
            Content: %s
            """;
    public static final Integer WORDS_PER_MINUTE = 200;
    public static final Integer EMAIL_VERIFICATION_EXPIRE_HOURS = 24;
    public static final int PASSWORD_RESET_EXPIRE_HOURS = 1;

    private CodeConstants() {
        throw new UnsupportedOperationException("Constant class cannot be instantiated");
    }
}
