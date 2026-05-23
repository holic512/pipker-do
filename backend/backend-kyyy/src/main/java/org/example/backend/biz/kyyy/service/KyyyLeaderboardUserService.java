/**
 * @file KyyyLeaderboardUserService
 * @project pipker-do
 * @module 考研英语 / 排行榜
 * @description 负责英语用户侧综合排行榜的实时聚合、缓存与个人名次查询。
 * @logic 1. 合并背词完成会话与阅读交卷数据生成综合榜；2. 按日榜、周榜、总榜输出榜单摘要与个人记录；3. 通过 Redis 缓存公共榜和个人榜降低重复聚合成本。
 * @dependencies Mapper: KyyyLeaderboardMapper, Storage: LocalFileStorage, Cache: StringRedisTemplate
 * @index_tags 考研英语, 排行榜服务, Redis缓存, 综合学习榜
 * @author holic512
 */
package org.example.backend.biz.kyyy.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.biz.kyyy.dto.KyyyLeaderboardAggregateRecord;
import org.example.backend.biz.kyyy.dto.KyyyLeaderboardRecordResponse;
import org.example.backend.biz.kyyy.dto.KyyyLeaderboardResponse;
import org.example.backend.biz.kyyy.dto.KyyyLeaderboardSummaryResponse;
import org.example.backend.biz.kyyy.mapper.KyyyLeaderboardMapper;
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
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
public class KyyyLeaderboardUserService {

    private static final Logger log = LoggerFactory.getLogger(KyyyLeaderboardUserService.class);

    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter PERIOD_TAG_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final String SCOPE_DAILY = "daily";
    private static final String SCOPE_WEEKLY = "weekly";
    private static final String SCOPE_TOTAL = "total";
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;
    private static final String CACHE_KEY_PREFIX = "kyyy:leaderboard";
    private static final Duration COMMON_LOCK_TTL = Duration.ofSeconds(8);
    private static final int COMMON_CACHE_RETRY_TIMES = 6;
    private static final long COMMON_CACHE_RETRY_SLEEP_MILLIS = 60L;
    private static final String RULE_DESCRIPTION = "排序规则：综合学习量优先，若相同则比综合正确率，再比最近活跃时间，最后按用户ID升序；学习量=完成背词卡片数+已作答阅读题数，正确数=认识单词数+答对阅读题数。";

    private final KyyyLeaderboardMapper kyyyLeaderboardMapper;
    private final LocalFileStorage localFileStorage;
    private final StringRedisTemplate stringRedisTemplate;

    public KyyyLeaderboardUserService(KyyyLeaderboardMapper kyyyLeaderboardMapper,
                                      LocalFileStorage localFileStorage,
                                      StringRedisTemplate stringRedisTemplate) {
        this.kyyyLeaderboardMapper = kyyyLeaderboardMapper;
        this.localFileStorage = localFileStorage;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public KyyyLeaderboardResponse getLeaderboard(Long userId, String scope, Integer limit) {
        String normalizedScope = normalizeScope(scope);
        int resolvedLimit = resolveLimit(limit);
        LocalDateTime generatedAt = LocalDateTime.now(SHANGHAI_ZONE);
        LeaderboardPeriod period = resolvePeriod(normalizedScope, generatedAt);

        CommonLeaderboardSnapshot commonSnapshot = loadCommonSnapshot(normalizedScope, period, resolvedLimit);
        List<KyyyLeaderboardAggregateRecord> topRecords = commonSnapshot.topRecords() == null ? List.of() : commonSnapshot.topRecords();
        List<KyyyLeaderboardRecordResponse> records = topRecords.stream()
                .map(item -> toRecordResponse(item, item.getRankNo(), userId))
                .toList();

        KyyyLeaderboardRecordResponse myRecord = records.stream()
                .filter(item -> Boolean.TRUE.equals(item.getIsMe()))
                .findFirst()
                .orElseGet(() -> loadUserRecord(normalizedScope, period, userId));
        KyyyLeaderboardSummaryResponse summary = commonSnapshot.summary() == null
                ? new KyyyLeaderboardSummaryResponse(
                normalizedScope,
                period.periodLabel(),
                period.periodStart(),
                period.periodEnd(),
                0,
                generatedAt,
                RULE_DESCRIPTION
        )
                : commonSnapshot.summary();
        return new KyyyLeaderboardResponse(summary, myRecord, records);
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
            log.warn("kyyy leaderboard common cache degraded, fallback to database", exception);
        }
        return queryCommonSnapshot(scope, period, limit);
    }

    private KyyyLeaderboardRecordResponse loadUserRecord(String scope, LeaderboardPeriod period, Long userId) {
        String cacheKey = buildUserCacheKey(scope, period.periodTag(), userId);
        try {
            UserCachePayload cachedPayload = readUserCachePayload(cacheKey);
            if (cachedPayload != null) {
                return toUserRecord(cachedPayload, userId);
            }
            KyyyLeaderboardRecordResponse databaseRecord = queryUserRecord(userId, period);
            writeUserCachePayload(cacheKey, fromUserRecord(databaseRecord), resolveUserCacheTtl(scope));
            return databaseRecord;
        } catch (Exception exception) {
            log.warn("kyyy leaderboard user cache degraded, fallback to database, userId={}", userId, exception);
            return queryUserRecord(userId, period);
        }
    }

    private KyyyLeaderboardRecordResponse queryUserRecord(Long userId, LeaderboardPeriod period) {
        KyyyLeaderboardAggregateRecord myAggregate = kyyyLeaderboardMapper.selectUserAggregateRecord(
                period.periodStart(),
                period.periodEnd(),
                userId
        );
        if (myAggregate == null) {
            return null;
        }
        Integer rankNo = kyyyLeaderboardMapper.selectUserRankNo(
                period.periodStart(),
                period.periodEnd(),
                userId
        );
        return toRecordResponse(myAggregate, rankNo, userId);
    }

    private CommonLeaderboardSnapshot queryCommonSnapshot(String scope, LeaderboardPeriod period, int limit) {
        List<KyyyLeaderboardAggregateRecord> topRecords = kyyyLeaderboardMapper.selectTopAggregateRecords(
                period.periodStart(),
                period.periodEnd(),
                limit
        );
        int participantCount = normalizeInteger(kyyyLeaderboardMapper.countParticipants(
                period.periodStart(),
                period.periodEnd()
        ));
        KyyyLeaderboardSummaryResponse response = new KyyyLeaderboardSummaryResponse(
                scope,
                period.periodLabel(),
                period.periodStart(),
                period.periodEnd(),
                participantCount,
                period.periodEnd(),
                RULE_DESCRIPTION
        );
        return new CommonLeaderboardSnapshot(response, topRecords == null ? List.of() : topRecords);
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
            log.warn("kyyy leaderboard cache lock release failed, key={}", lockKey, exception);
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
        KyyyLeaderboardSummaryResponse summary = snapshot.summary();
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
        KyyyLeaderboardSummaryResponse summary = new KyyyLeaderboardSummaryResponse(
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

    private UserCachePayload fromUserRecord(KyyyLeaderboardRecordResponse record) {
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

    private KyyyLeaderboardRecordResponse toUserRecord(UserCachePayload payload, Long userId) {
        if (!payload.hasRecord) {
            return null;
        }
        return new KyyyLeaderboardRecordResponse(
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
            throw new IllegalStateException("kyyy leaderboard cache payload serialize failed", exception);
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
            log.warn("kyyy leaderboard cache payload parse failed, fallback to db. encoded={}", encodedValue, exception);
            return null;
        }
    }

    private KyyyLeaderboardRecordResponse toRecordResponse(KyyyLeaderboardAggregateRecord source,
                                                           Integer rankNo,
                                                           Long userId) {
        return new KyyyLeaderboardRecordResponse(
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

    private record CommonLeaderboardSnapshot(KyyyLeaderboardSummaryResponse summary,
                                             List<KyyyLeaderboardAggregateRecord> topRecords) {
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

        private List<KyyyLeaderboardAggregateRecord> topRecords;
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
