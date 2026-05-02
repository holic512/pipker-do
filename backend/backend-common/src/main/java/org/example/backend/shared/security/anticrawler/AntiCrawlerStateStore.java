package org.example.backend.shared.security.anticrawler;

import java.time.Duration;

/**
 * AI 索引: 反扒状态存储抽象。
 */
public interface AntiCrawlerStateStore {

    long incrementCounter(String key, Duration ttl);

    void setExpiringValue(String key, String value, Duration ttl);

    ExpiringValue getExpiringValue(String key);

    long addSetMemberAndCount(String key, String member, Duration ttl);

    record ExpiringValue(String value, long ttlSeconds) {
    }
}
