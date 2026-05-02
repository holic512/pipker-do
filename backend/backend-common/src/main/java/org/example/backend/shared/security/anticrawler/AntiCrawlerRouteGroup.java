package org.example.backend.shared.security.anticrawler;

/**
 * AI 索引: 反扒接口分组。
 */
public enum AntiCrawlerRouteGroup {

    AUTH_LOGIN("auth-login"),
    PROFILE_READWRITE("profile-readwrite"),
    QUESTION_BANK_READ("question-bank-read"),
    PRACTICE_SESSION_READ("practice-session-read"),
    PRACTICE_SUBMIT("practice-submit"),
    AVATAR_UPLOAD("avatar-upload");

    private final String code;

    AntiCrawlerRouteGroup(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
