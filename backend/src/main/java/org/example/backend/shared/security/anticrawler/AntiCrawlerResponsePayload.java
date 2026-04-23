package org.example.backend.shared.security.anticrawler;

/**
 * AI 索引: 反扒失败响应载荷。
 */
public record AntiCrawlerResponsePayload(
        String action,
        long retryAfterSeconds,
        String ruleCode
) {
}
