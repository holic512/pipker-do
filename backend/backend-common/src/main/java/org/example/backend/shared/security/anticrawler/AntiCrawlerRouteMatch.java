package org.example.backend.shared.security.anticrawler;

/**
 * AI 索引: 反扒路由命中结果。
 */
public record AntiCrawlerRouteMatch(
        AntiCrawlerRouteGroup group,
        String normalizedPath,
        boolean readOperation
) {
}
