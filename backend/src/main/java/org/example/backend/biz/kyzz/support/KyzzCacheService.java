package org.example.backend.biz.kyzz.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * AI 索引: KYZZ Redis 简单缓存服务。
 */
@Service
public class KyzzCacheService {

    public static final String CATEGORY_LIST_KEY = "kyzz:category:list";
    public static final String ACTIVE_BANK_LIST_KEY = "kyzz:bank:active:list";

    public static final Duration PUBLIC_BASE_TTL = Duration.ofMinutes(20);
    public static final Duration USER_AGGREGATE_TTL = Duration.ofSeconds(60);
    public static final Duration COMMENT_FIRST_PAGE_TTL = Duration.ofSeconds(30);

    private static final Logger log = LoggerFactory.getLogger(KyzzCacheService.class);

    private final StringRedisTemplate stringRedisTemplate;

    public KyzzCacheService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public String bankQuestionsKey(Long bankId) {
        return "kyzz:bank:" + bankId + ":questions";
    }

    public String questionOptionsKey(Long questionId) {
        return "kyzz:question:" + questionId + ":options";
    }

    public String userMineBanksKey(Long userId) {
        return "kyzz:user:" + userId + ":banks:mine";
    }

    public String userDashboardKey(Long userId) {
        return "kyzz:user:" + userId + ":dashboard";
    }

    public String userWrongQuestionsKey(Long userId, String status, String keyword) {
        return "kyzz:user:" + userId + ":wrong:" + normalizeKeyPart(status) + ":" + normalizeKeyPart(keyword);
    }

    public String userFavoriteQuestionsKey(Long userId, String keyword) {
        return "kyzz:user:" + userId + ":favorite:" + normalizeKeyPart(keyword);
    }

    public String questionCommentsKey(Long questionId, Long userId, long pageNo, long pageSize) {
        return "kyzz:question:" + questionId + ":comments:p" + pageNo + ":s" + pageSize + ":uid:" + userId;
    }

    public <T extends Serializable> T getOrLoad(String key, Duration ttl, Class<T> valueType, Supplier<T> loader) {
        T cachedValue = get(key, valueType);
        if (cachedValue != null) {
            return cachedValue;
        }
        T loadedValue = loader.get();
        put(key, loadedValue, ttl);
        return loadedValue;
    }

    public <T> T get(String key, Class<T> valueType) {
        try {
            String encodedValue = stringRedisTemplate.opsForValue().get(key);
            if (!StringUtils.hasText(encodedValue)) {
                return null;
            }
            return deserializeFromBase64(encodedValue, valueType);
        } catch (Exception exception) {
            log.warn("kyzz cache read failed, key={}", key, exception);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Serializable> List<T> getList(String key) {
        ArrayList<?> cachedList = get(key, ArrayList.class);
        if (cachedList == null) {
            return null;
        }
        return (List<T>) cachedList;
    }

    public void putList(String key, List<? extends Serializable> values, Duration ttl) {
        if (values == null) {
            return;
        }
        put(key, new ArrayList<>(values), ttl);
    }

    public void put(String key, Serializable value, Duration ttl) {
        if (value == null) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue().set(key, serializeToBase64(value), ttl);
        } catch (Exception exception) {
            log.warn("kyzz cache write failed, key={}", key, exception);
        }
    }

    public void delete(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception exception) {
            log.warn("kyzz cache delete failed, key={}", key, exception);
        }
    }

    public void deleteByPattern(String pattern) {
        try {
            Set<String> keys = stringRedisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
        } catch (Exception exception) {
            log.warn("kyzz cache delete pattern failed, pattern={}", pattern, exception);
        }
    }

    public void evictPublicBaseCaches() {
        delete(CATEGORY_LIST_KEY);
        delete(ACTIVE_BANK_LIST_KEY);
        deleteByPattern("kyzz:bank:*:questions");
        deleteByPattern("kyzz:question:*:options");
    }

    public void evictUserAggregateCaches(Long userId) {
        if (userId == null) {
            return;
        }
        delete(userMineBanksKey(userId));
        delete(userDashboardKey(userId));
        deleteByPattern("kyzz:user:" + userId + ":wrong:*");
        evictUserFavoriteCaches(userId);
    }

    public void evictUserFavoriteCaches(Long userId) {
        if (userId == null) {
            return;
        }
        deleteByPattern("kyzz:user:" + userId + ":favorite:*");
    }

    public void evictQuestionCommentCaches(Long questionId) {
        if (questionId == null) {
            return;
        }
        deleteByPattern("kyzz:question:" + questionId + ":comments:*");
    }

    private String normalizeKeyPart(String value) {
        if (!StringUtils.hasText(value)) {
            return "_";
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.trim().getBytes());
    }

    private String serializeToBase64(Serializable value) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(value);
            objectOutputStream.flush();
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException exception) {
            throw new IllegalStateException("kyzz cache payload serialize failed", exception);
        }
    }

    private <T> T deserializeFromBase64(String encodedValue, Class<T> valueType) {
        try {
            byte[] bytes = Base64.getDecoder().decode(encodedValue);
            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                 ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
                Object decodedObject = objectInputStream.readObject();
                if (!valueType.isInstance(decodedObject)) {
                    return null;
                }
                return valueType.cast(decodedObject);
            }
        } catch (Exception exception) {
            log.warn("kyzz cache payload parse failed", exception);
            return null;
        }
    }
}
