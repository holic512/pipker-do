package org.example.backend.shared.security.anticrawler;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * AI 索引: 反扒接口分组匹配器。
 */
@Component
public class AntiCrawlerRouteClassifier {

    private static final Pattern QUESTION_BANK_SELECTION_PATH = Pattern.compile("^/api/kyzz/question-banks/\\d+/selection$");
    private static final Pattern PRACTICE_ANSWER_PREVIEW_PATH = Pattern.compile("^/api/kyzz/practice/questions/\\d+/answer-preview$");
    private static final Pattern PRACTICE_REVIEW_PATH = Pattern.compile("^/api/kyzz/practice/questions/\\d+/review$");
    private static final Pattern PRACTICE_SELF_JUDGEMENT_PATH = Pattern.compile("^/api/kyzz/practice/questions/\\d+/self-judgement$");
    private static final Pattern KYYY_PRACTICE_FEEDBACK_PATH = Pattern.compile("^/api/kyyy/practice/session/\\d+/feedback$");

    public AntiCrawlerRouteMatch match(String path, String method) {
        if ("POST".equalsIgnoreCase(method) && "/api/auth/wechat/login".equals(path)) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.AUTH_LOGIN, "/api/auth/wechat/login", false);
        }
        if ("POST".equalsIgnoreCase(method) && "/api/auth/logout".equals(path)) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.PROFILE_READWRITE, "/api/auth/logout", false);
        }
        if ("GET".equalsIgnoreCase(method) && "/api/user/me".equals(path)) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.PROFILE_READWRITE, "/api/user/me", true);
        }
        if ("GET".equalsIgnoreCase(method) && "/api/user/default-project".equals(path)) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.PROFILE_READWRITE, "/api/user/default-project", true);
        }
        if ("PUT".equalsIgnoreCase(method) && "/api/user/profile".equals(path)) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.PROFILE_READWRITE, "/api/user/profile", false);
        }
        if ("PUT".equalsIgnoreCase(method) && "/api/user/default-project".equals(path)) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.PROFILE_READWRITE, "/api/user/default-project", false);
        }
        if ("GET".equalsIgnoreCase(method) && "/api/kyzz/question-banks/mine".equals(path)) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.QUESTION_BANK_READ, "/api/kyzz/question-banks/mine", true);
        }
        if ("GET".equalsIgnoreCase(method) && "/api/kyzz/question-banks/public".equals(path)) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.QUESTION_BANK_READ, "/api/kyzz/question-banks/public", true);
        }
        if ("GET".equalsIgnoreCase(method) && "/api/kyzz/practice/dashboard".equals(path)) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.QUESTION_BANK_READ, "/api/kyzz/practice/dashboard", true);
        }
        if ("GET".equalsIgnoreCase(method) && "/api/kyzz/practice/session".equals(path)) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.PRACTICE_SESSION_READ, "/api/kyzz/practice/session", true);
        }
        if ("GET".equalsIgnoreCase(method) && "/api/kyzz/practice/settings".equals(path)) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.PRACTICE_SESSION_READ, "/api/kyzz/practice/settings", true);
        }
        if ("GET".equalsIgnoreCase(method) && "/api/kyyy/practice/session".equals(path)) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.PRACTICE_SESSION_READ, "/api/kyyy/practice/session", true);
        }
        if ("GET".equalsIgnoreCase(method) && "/api/kyyy/practice/settings".equals(path)) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.PRACTICE_SESSION_READ, "/api/kyyy/practice/settings", true);
        }
        if ("GET".equalsIgnoreCase(method) && PRACTICE_ANSWER_PREVIEW_PATH.matcher(path).matches()) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.PRACTICE_SESSION_READ, "/api/kyzz/practice/questions/{questionId}/answer-preview", true);
        }
        if ("PUT".equalsIgnoreCase(method) && "/api/kyzz/practice/settings".equals(path)) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.PROFILE_READWRITE, "/api/kyzz/practice/settings", false);
        }
        if ("PUT".equalsIgnoreCase(method) && "/api/kyyy/practice/settings".equals(path)) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.PROFILE_READWRITE, "/api/kyyy/practice/settings", false);
        }
        if ("POST".equalsIgnoreCase(method) && "/api/kyzz/practice/progress/reset".equals(path)) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.PRACTICE_SUBMIT, "/api/kyzz/practice/progress/reset", false);
        }
        if ("POST".equalsIgnoreCase(method) && KYYY_PRACTICE_FEEDBACK_PATH.matcher(path).matches()) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.PRACTICE_SUBMIT, "/api/kyyy/practice/session/{sessionId}/feedback", false);
        }
        if ("GET".equalsIgnoreCase(method) && "/api/kyzz/leaderboard".equals(path)) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.QUESTION_BANK_READ, "/api/kyzz/leaderboard", true);
        }
        if ("GET".equalsIgnoreCase(method) && "/api/kyyy/leaderboard".equals(path)) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.QUESTION_BANK_READ, "/api/kyyy/leaderboard", true);
        }
        if ("PUT".equalsIgnoreCase(method) && QUESTION_BANK_SELECTION_PATH.matcher(path).matches()) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.PRACTICE_SUBMIT, "/api/kyzz/question-banks/{bankId}/selection", false);
        }
        if ("POST".equalsIgnoreCase(method) && PRACTICE_REVIEW_PATH.matcher(path).matches()) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.PRACTICE_SUBMIT, "/api/kyzz/practice/questions/{questionId}/review", false);
        }
        if ("POST".equalsIgnoreCase(method) && PRACTICE_SELF_JUDGEMENT_PATH.matcher(path).matches()) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.PRACTICE_SUBMIT, "/api/kyzz/practice/questions/{questionId}/self-judgement", false);
        }
        if ("POST".equalsIgnoreCase(method) && "/api/files/avatar".equals(path)) {
            return new AntiCrawlerRouteMatch(AntiCrawlerRouteGroup.AVATAR_UPLOAD, "/api/files/avatar", false);
        }
        return null;
    }
}
