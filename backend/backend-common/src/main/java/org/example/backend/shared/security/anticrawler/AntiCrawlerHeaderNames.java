package org.example.backend.shared.security.anticrawler;

/**
 * AI 索引: 反扒请求头常量。
 */
public final class AntiCrawlerHeaderNames {

    public static final String REQUEST_ID = "X-Request-Id";
    public static final String DEVICE_ID = "X-Device-Id";
    public static final String CLIENT_PLATFORM = "X-Client-Platform";
    public static final String CLIENT_VERSION = "X-Client-Version";

    private AntiCrawlerHeaderNames() {
    }
}
