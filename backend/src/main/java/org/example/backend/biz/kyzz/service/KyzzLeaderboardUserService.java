package org.example.backend.biz.kyzz.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.biz.kyzz.dto.KyzzLeaderboardAggregateRecord;
import org.example.backend.biz.kyzz.dto.KyzzLeaderboardRecordResponse;
import org.example.backend.biz.kyzz.dto.KyzzLeaderboardResponse;
import org.example.backend.biz.kyzz.dto.KyzzLeaderboardSummaryResponse;
import org.example.backend.biz.kyzz.mapper.KyzzLeaderboardMapper;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.storage.service.LocalFileStorage;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.Base64;

/**
 * AI 索引: KYZZ 用户侧排行榜服务。
 */
@Service
public class KyzzLeaderboardUserService {

    private static final Logger log = LoggerFactory.getLogger(KyzzLeaderboardUserService.class);

    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter PERIOD_TAG_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final String SCOPE_DAILY = "daily";
    private static final String SCOPE_WEEKLY = "weekly";
    private static final String SCOPE_TOTAL = "total";
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;
    private static final String CACHE_KEY_PREFIX = "kyzz:leaderboard";
    private static final Duration COMMON_LOCK_TTL = Duration.ofSeconds(8);
    private static final int COMMON_CACHE_RETRY_TIMES = 6;
    private static final long COMMON_CACHE_RETRY_SLEEP_MILLIS = 60L;
    private static final String RULE_DESCRIPTION = "排序规则：做题量优先，若相同则比正确率，再比最近活跃时间，最后按用户ID升序；做题数=有效作答次数。";

    private final KyzzLeaderboardMapper kyzzLeaderboardMapper;
    private final LocalFileStorage localFileStorage;
    private final StringRedisTemplate stringRedisTemplate;

    public KyzzLeaderboardUserService(KyzzLeaderboardMapper kyzzLeaderboardMapper,
                                      LocalFileStorage localFileStorage,
                                      StringRedisTemplate stringRedisTemplate) {
        this.kyzzLeaderboardMapper = kyzzLeaderboardMapper;
        this.localFileStorage = localFileStorage;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public KyzzLeaderboardResponse getLeaderboard(Long userId, String scope, Integer limit) {
        String normalizedScope = normalizeScope(scope);
        int resolvedLimit = resolveLimit(limit);
        LocalDateTime generatedAt = LocalDateTime.now(SHANGHAI_ZONE);
        LeaderboardPeriod period = resolvePeriod(normalizedScope, generatedAt);

        CommonLeaderboardSnapshot commonSnapshot = loadCommonSnapshot(normalizedScope, period, resolvedLimit);
        List<KyzzLeaderboardAggregateRecord> topRecords = commonSnapshot.topRecords() == null ? List.of() : commonSnapshot.topRecords();
        List<KyzzLeaderboardRecordResponse> records = topRecords.stream()
                .map(item -> toRecordResponse(item, item.getRankNo(), userId))
                .toList();

        KyzzLeaderboardRecordResponse myRecord = records.stream()
                .filter(item -> Boolean.TRUE.equals(item.getIsMe()))
                .findFirst()
                .orElseGet(() -> loadUserRecord(normalizedScope, period, userId));
        KyzzLeaderboardSummaryResponse summary = commonSnapshot.summary() == null
                ? new KyzzLeaderboardSummaryResponse(
                normalizedScope,
                period.periodLabel(),
                period.periodStart(),
                period.periodEnd(),
                0,
                generatedAt,
                RULE_DESCRIPTION
        )
                : commonSnapshot.summary();
        return new KyzzLeaderboardResponse(summary, myRecord, records);
    }

    private CommonLeaderboardSnapshot loadCommonSnapshot(String scope, LeaderboardPeriod period, int limit) {
        String cacheKey = buildCommonCacheKey(scope, period.periodTag(), limit);
        String lockKey = cacheKey + ":lock";
        try {
            CommonCachePayload cachedPayload = readCommonCachePayload(cacheKey);
            if (cachedPayload != null) {
                return toCommonSnapshot(cachedPayload);
            }
            String lockToken = UUID.randomUUID().toString();
            if (acquireLock(lockKey, lockToken)) {
                try {
                    CommonCachePayload doubleCheckedPayload = readCommonCachePayload(cacheKey);
                    if (doubleCheckedPayload != null) {
                        return toCommonSnapshot(doubleCheckedPayload);
                    }
                    CommonLeaderboardSnapshot freshSnapshot = queryCommonSnapshot(scope, period, limit);
                    writeCommonCachePayload(cacheKey, fromCommonSnapshot(freshSnapshot), resolveCommonCacheTtl(scope));
                    return freshSnapshot;
                } finally {
                    releaseLock(lockKey, lockToken);
                }
            }
            for (int index = 0; index < COMMON_CACHE_RETRY_TIMES; index++) {
                sleepQuietly(COMMON_CACHE_RETRY_SLEEP_MILLIS);
                CommonCachePayload retryPayload = readCommonCachePayload(cacheKey);
                if (retryPayload != null) {
                    return toCommonSnapshot(retryPayload);
                }
            }
        } catch (Exception exception) {
            log.warn("leaderboard common cache degraded, fallback to database", exception);
        }
        return queryCommonSnapshot(scope, period, limit);
    }

    private KyzzLeaderboardRecordResponse loadUserRecord(String scope, LeaderboardPeriod period, Long userId) {
        String cacheKey = buildUserCacheKey(scope, period.periodTag(), userId);
        try {
            UserCachePayload cachedPayload = readUserCachePayload(cacheKey);
            if (cachedPayload != null) {
                return toUserRecord(cachedPayload, userId);
            }
            KyzzLeaderboardRecordResponse databaseRecord = queryUserRecord(userId, period);
            writeUserCachePayload(cacheKey, fromUserRecord(databaseRecord), resolveUserCacheTtl(scope));
            return databaseRecord;
        } catch (Exception exception) {
            log.warn("leaderboard user cache degraded, fallback to database, userId={}", userId, exception);
            return queryUserRecord(userId, period);
        }
    }

    private KyzzLeaderboardRecordResponse queryUserRecord(Long userId, LeaderboardPeriod period) {
        KyzzLeaderboardAggregateRecord myAggregate = kyzzLeaderboardMapper.selectUserAggregateRecord(
                period.periodStart(),
                period.periodEnd(),
                userId
        );
        if (myAggregate == null) {
            return null;
        }
        Integer rankNo = kyzzLeaderboardMapper.selectUserRankNo(
                period.periodStart(),
                period.periodEnd(),
                userId
        );
        return toRecordResponse(myAggregate, rankNo, userId);
    }

    private CommonLeaderboardSnapshot queryCommonSnapshot(String scope, LeaderboardPeriod period, int limit) {
        List<KyzzLeaderboardAggregateRecord> topRecords = kyzzLeaderboardMapper.selectTopAggregateRecords(
                period.periodStart(),
                period.periodEnd(),
                limit
        );
        int participantCount = normalizeInteger(kyzzLeaderboardMapper.countParticipants(
                period.periodStart(),
                period.periodEnd()
        ));
        KyzzLeaderboardSummaryResponse summary = new KyzzLeaderboardSummaryResponse(
                scope,
                period.periodLabel(),
                period.periodStart(),
                period.periodEnd(),
                participantCount,
                period.periodEnd(),
                RULE_DESCRIPTION
        );
        return new CommonLeaderboardSnapshot(summary, topRecords == null ? List.of() : topRecords);
    }

    private String buildCommonCacheKey(String scope, String periodTag, int limit) {
        return CACHE_KEY_PREFIX + ":common:" + scope + ":" + periodTag + ":limit:" + limit;
    }

    private String buildUserCacheKey(String scope, String periodTag, Long userId) {
        return CACHE_KEY_PREFIX + ":user:" + scope + ":" + periodTag + ":uid:" + userId;
    }

    private CommonCachePayload readCommonCachePayload(String cacheKey) {
        String encodedPayload = stringRedisTemplate.opsForValue().get(cacheKey);
        if (!StringUtils.hasText(encodedPayload)) {
            return null;
        }
        return deserializeFromBase64(encodedPayload, CommonCachePayload.class);
    }

    private void writeCommonCachePayload(String cacheKey, CommonCachePayload payload, Duration ttl) {
        stringRedisTemplate.opsForValue().set(cacheKey, serializeToBase64(payload), ttl);
    }

    private UserCachePayload readUserCachePayload(String cacheKey) {
        String encodedPayload = stringRedisTemplate.opsForValue().get(cacheKey);
        if (!StringUtils.hasText(encodedPayload)) {
            return null;
        }
        return deserializeFromBase64(encodedPayload, UserCachePayload.class);
    }

    private void writeUserCachePayload(String cacheKey, UserCachePayload payload, Duration ttl) {
        stringRedisTemplate.opsForValue().set(cacheKey, serializeToBase64(payload), ttl);
    }

    private boolean acquireLock(String lockKey, String lockToken) {
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, lockToken, COMMON_LOCK_TTL);
        return Boolean.TRUE.equals(locked);
    }

    private void releaseLock(String lockKey, String lockToken) {
        try {
            String currentToken = stringRedisTemplate.opsForValue().get(lockKey);
            if (Objects.equals(currentToken, lockToken)) {
                stringRedisTemplate.delete(lockKey);
            }
        } catch (Exception exception) {
            log.warn("leaderboard cache lock release failed, key={}", lockKey, exception);
        }
    }

    private Duration resolveCommonCacheTtl(String scope) {
        if (SCOPE_DAILY.equals(scope)) {
            return Duration.ofSeconds(30);
        }
        if (SCOPE_WEEKLY.equals(scope)) {
            return Duration.ofSeconds(45);
        }
        return Duration.ofSeconds(90);
    }

    private Duration resolveUserCacheTtl(String scope) {
        if (SCOPE_TOTAL.equals(scope)) {
            return Duration.ofSeconds(120);
        }
        return Duration.ofSeconds(45);
    }

    private CommonCachePayload fromCommonSnapshot(CommonLeaderboardSnapshot snapshot) {
        KyzzLeaderboardSummaryResponse summary = snapshot.summary();
        return new CommonCachePayload(
                summary == null ? null : summary.getScope(),
                summary == null ? null : summary.getPeriodLabel(),
                summary == null ? null : summary.getPeriodStart(),
                summary == null ? null : summary.getPeriodEnd(),
                summary == null ? null : summary.getParticipantCount(),
                summary == null ? null : summary.getGeneratedAt(),
                summary == null ? null : summary.getRuleDescription(),
                snapshot.topRecords()
        );
    }

    private CommonLeaderboardSnapshot toCommonSnapshot(CommonCachePayload payload) {
        KyzzLeaderboardSummaryResponse summary = new KyzzLeaderboardSummaryResponse(
                payload.scope,
                payload.periodLabel,
                payload.periodStart,
                payload.periodEnd,
                payload.participantCount == null ? 0 : payload.participantCount,
                payload.generatedAt,
                StringUtils.hasText(payload.ruleDescription) ? payload.ruleDescription : RULE_DESCRIPTION
        );
        return new CommonLeaderboardSnapshot(summary, payload.topRecords == null ? List.of() : payload.topRecords);
    }

    private UserCachePayload fromUserRecord(KyzzLeaderboardRecordResponse record) {
        if (record == null) {
            return new UserCachePayload(false, null, null, null, null, null, null, null, null);
        }
        return new UserCachePayload(
                true,
                record.getRankNo(),
                record.getUserId(),
                record.getNickname(),
                record.getAvatarUrl(),
                record.getStudyCount(),
                record.getCorrectCount(),
                record.getAccuracyRate(),
                record.getLastPracticeAt()
        );
    }

    private KyzzLeaderboardRecordResponse toUserRecord(UserCachePayload payload, Long userId) {
        if (!payload.hasRecord) {
            return null;
        }
        return new KyzzLeaderboardRecordResponse(
                normalizeInteger(payload.rankNo),
                payload.userId,
                normalizeNickname(payload.nickname),
                resolveAvatarUrl(payload.avatarUrl),
                normalizeInteger(payload.studyCount),
                normalizeInteger(payload.correctCount),
                normalizeAccuracyRate(payload.accuracyRate),
                payload.lastPracticeAt,
                Objects.equals(payload.userId, userId)
        );
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private String serializeToBase64(Serializable value) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(value);
            objectOutputStream.flush();
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException exception) {
            throw new IllegalStateException("leaderboard cache payload serialize failed", exception);
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
            log.warn("leaderboard cache payload parse failed, fallback to db. encoded={}", encodedValue, exception);
            return null;
        }
    }

    private KyzzLeaderboardRecordResponse toRecordResponse(KyzzLeaderboardAggregateRecord source,
                                                           Integer rankNo,
                                                           Long userId) {
        return new KyzzLeaderboardRecordResponse(
                normalizeInteger(rankNo),
                source.getUserId(),
                normalizeNickname(source.getNickname()),
                resolveAvatarUrl(source.getAvatarUrl()),
                normalizeInteger(source.getStudyCount()),
                normalizeInteger(source.getCorrectCount()),
                normalizeAccuracyRate(source.getAccuracyRate()),
                source.getLastPracticeAt(),
                Objects.equals(source.getUserId(), userId)
        );
    }

    private String normalizeScope(String scope) {
        if (!StringUtils.hasText(scope)) {
            return SCOPE_DAILY;
        }
        String normalized = scope.trim().toLowerCase(Locale.ROOT);
        if (SCOPE_DAILY.equals(normalized) || SCOPE_WEEKLY.equals(normalized) || SCOPE_TOTAL.equals(normalized)) {
            return normalized;
        }
        throw new BusinessException(ApiResponseCode.BAD_REQUEST, "scope 仅支持 daily、weekly、total");
    }

    private int resolveLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit <= 0) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "limit 需要大于 0");
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private LeaderboardPeriod resolvePeriod(String scope, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        if (SCOPE_DAILY.equals(scope)) {
            LocalDateTime start = today.atStartOfDay();
            return new LeaderboardPeriod(
                    today.format(DATE_FORMATTER) + " 当日",
                    start,
                    now,
                    "d" + today.format(PERIOD_TAG_FORMATTER)
            );
        }
        if (SCOPE_WEEKLY.equals(scope)) {
            LocalDate weekStartDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDateTime start = weekStartDate.atStartOfDay();
            return new LeaderboardPeriod(
                    weekStartDate.format(DATE_FORMATTER) + " 至 " + today.format(DATE_FORMATTER),
                    start,
                    now,
                    "w" + weekStartDate.format(PERIOD_TAG_FORMATTER)
            );
        }
        return new LeaderboardPeriod("历史累计", null, now, "all");
    }

    private int normalizeInteger(Number value) {
        return value == null ? 0 : value.intValue();
    }

    private BigDecimal normalizeAccuracyRate(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal normalized = value.setScale(2, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (normalized.compareTo(BigDecimal.valueOf(100)) > 0) {
            return BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);
        }
        return normalized;
    }

    private String normalizeNickname(String nickname) {
        return StringUtils.hasText(nickname) ? nickname.trim() : "匿名用户";
    }

    private String resolveAvatarUrl(String avatarValue) {
        if (!StringUtils.hasText(avatarValue)) {
            return null;
        }
        if (localFileStorage.isManagedKey(avatarValue)) {
            return localFileStorage.resolveUrl(avatarValue);
        }
        return avatarValue;
    }

    private record LeaderboardPeriod(String periodLabel,
                                     LocalDateTime periodStart,
                                     LocalDateTime periodEnd,
                                     String periodTag) {
    }

    private record CommonLeaderboardSnapshot(KyzzLeaderboardSummaryResponse summary,
                                             List<KyzzLeaderboardAggregateRecord> topRecords) {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class CommonCachePayload implements Serializable {

        private static final long serialVersionUID = 1L;

        private String scope;

        private String periodLabel;

        private LocalDateTime periodStart;

        private LocalDateTime periodEnd;

        private Integer participantCount;

        private LocalDateTime generatedAt;

        private String ruleDescription;

        private List<KyzzLeaderboardAggregateRecord> topRecords;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class UserCachePayload implements Serializable {

        private static final long serialVersionUID = 1L;

        private boolean hasRecord;

        private Integer rankNo;

        private Long userId;

        private String nickname;

        private String avatarUrl;

        private Integer studyCount;

        private Integer correctCount;

        private BigDecimal accuracyRate;

        private LocalDateTime lastPracticeAt;
    }
}
