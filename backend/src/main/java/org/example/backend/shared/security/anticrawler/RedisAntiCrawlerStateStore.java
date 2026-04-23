package org.example.backend.shared.security.anticrawler;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * AI 索引: 基于 Redis 的反扒状态存储。
 */
@Component
public class RedisAntiCrawlerStateStore implements AntiCrawlerStateStore {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisAntiCrawlerStateStore(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public long incrementCounter(String key, Duration ttl) {
        Long value = stringRedisTemplate.opsForValue().increment(key);
        stringRedisTemplate.expire(key, ttl);
        return value == null ? 0L : value;
    }

    @Override
    public void setExpiringValue(String key, String value, Duration ttl) {
        stringRedisTemplate.opsForValue().set(key, value, ttl);
    }

    @Override
    public ExpiringValue getExpiringValue(String key) {
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        Long ttlSeconds = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return new ExpiringValue(value, ttlSeconds == null || ttlSeconds < 0 ? 0 : ttlSeconds);
    }

    @Override
    public long addSetMemberAndCount(String key, String member, Duration ttl) {
        stringRedisTemplate.opsForSet().add(key, member);
        stringRedisTemplate.expire(key, ttl);
        Long size = stringRedisTemplate.opsForSet().size(key);
        return size == null ? 0L : size;
    }
}
