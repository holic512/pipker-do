package org.example.backend.shared.security.anticrawler;

import org.example.backend.common.api.ApiResponseCode;

/**
 * AI 索引: 反扒决策结果。
 */
public record AntiCrawlerDecision(
        AntiCrawlerAction action,
        String ruleCode,
        long retryAfterSeconds,
        String message,
        int httpStatus,
        int responseCode,
        int riskScore
) {

    public static AntiCrawlerDecision allow() {
        return new AntiCrawlerDecision(null, null, 0, null, 200, ApiResponseCode.SUCCESS, 0);
    }

    public static AntiCrawlerDecision observe(String ruleCode, int riskScore) {
        return new AntiCrawlerDecision(AntiCrawlerAction.OBSERVE, ruleCode, 0, null, 200, ApiResponseCode.SUCCESS, riskScore);
    }

    public static AntiCrawlerDecision cooldown(String ruleCode, long retryAfterSeconds, int riskScore) {
        return new AntiCrawlerDecision(
                AntiCrawlerAction.COOLDOWN,
                ruleCode,
                retryAfterSeconds,
                "请求过于频繁，请稍后再试",
                429,
                ApiResponseCode.RATE_LIMITED,
                riskScore
        );
    }

    public static AntiCrawlerDecision blocked(String ruleCode, long retryAfterSeconds, int riskScore) {
        return new AntiCrawlerDecision(
                AntiCrawlerAction.BLOCKED,
                ruleCode,
                retryAfterSeconds,
                "访问行为异常，已暂时限制访问",
                403,
                ApiResponseCode.RISK_BLOCKED,
                riskScore
        );
    }

    public boolean triggered() {
        return action == AntiCrawlerAction.COOLDOWN || action == AntiCrawlerAction.BLOCKED;
    }

    public boolean strongerThan(AntiCrawlerDecision other) {
        return priority() > other.priority();
    }

    public AntiCrawlerResponsePayload toPayload() {
        return new AntiCrawlerResponsePayload(action == null ? null : action.value(), retryAfterSeconds, ruleCode);
    }

    private int priority() {
        if (action == AntiCrawlerAction.BLOCKED) {
            return 3;
        }
        if (action == AntiCrawlerAction.COOLDOWN) {
            return 2;
        }
        if (action == AntiCrawlerAction.OBSERVE) {
            return 1;
        }
        return 0;
    }
}
