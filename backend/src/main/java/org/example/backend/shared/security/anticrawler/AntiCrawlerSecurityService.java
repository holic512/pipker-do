package org.example.backend.shared.security.anticrawler;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 索引: 小程序用户接口反扒核心服务。
 */
@Service
public class AntiCrawlerSecurityService {

    private static final Logger log = LoggerFactory.getLogger(AntiCrawlerSecurityService.class);
    private static final String RULE_CODE_REDIS_OBSERVE_ONLY = "anti-crawler-observe-only";
    private static final String RULE_CODE_SUSPICIOUS_FINGERPRINT = "suspicious-client-fingerprint";
    private static final String RULE_CODE_MULTI_IP_SWITCH = "account-multi-ip-switch";
    private static final String RULE_CODE_SESSION_QUESTION_SCAN = "practice-session-question-scan";
    private static final String RULE_CODE_SESSION_BANK_SCAN = "practice-session-bank-scan";
    private static final String RULE_CODE_BOT_SUBMIT = "practice-submit-bot";
    private static final Pattern PRACTICE_QUESTION_PATH = Pattern.compile("^/api/kyzz/practice/questions/(\\d+)/.*$");

    private final AntiCrawlerProperties antiCrawlerProperties;
    private final AntiCrawlerRouteClassifier antiCrawlerRouteClassifier;
    private final AntiCrawlerStateStore antiCrawlerStateStore;
    private final AntiCrawlerRiskEventService antiCrawlerRiskEventService;
    private final Clock antiCrawlerClock;

    public AntiCrawlerSecurityService(AntiCrawlerProperties antiCrawlerProperties,
                                      AntiCrawlerRouteClassifier antiCrawlerRouteClassifier,
                                      AntiCrawlerStateStore antiCrawlerStateStore,
                                      AntiCrawlerRiskEventService antiCrawlerRiskEventService,
                                      Clock antiCrawlerClock) {
        this.antiCrawlerProperties = antiCrawlerProperties;
        this.antiCrawlerRouteClassifier = antiCrawlerRouteClassifier;
        this.antiCrawlerStateStore = antiCrawlerStateStore;
        this.antiCrawlerRiskEventService = antiCrawlerRiskEventService;
        this.antiCrawlerClock = antiCrawlerClock;
    }

    public void enforceLoginRequest(HttpServletRequest request) {
        if (!antiCrawlerProperties.isEnabled()) {
            return;
        }
        AntiCrawlerRouteMatch routeMatch = antiCrawlerRouteClassifier.match(request.getRequestURI(), request.getMethod());
        if (routeMatch == null || routeMatch.group() != AntiCrawlerRouteGroup.AUTH_LOGIN) {
            return;
        }
        RequestFingerprint fingerprint = RequestFingerprintHolder.get();
        if (isWhitelistRequest(fingerprint)) {
            return;
        }
        List<RateIdentity> identities = List.of(
                new RateIdentity("ip", sanitizeForKey("ip:" + valueOrDefault(fingerprint == null ? null : fingerprint.clientIp(), "unknown")
                        + ":route:" + routeMatch.normalizedPath())),
                new RateIdentity("device", sanitizeForKey("device:" + valueOrDefault(resolveDeviceId(fingerprint), "missing")
                        + ":route:" + routeMatch.normalizedPath()))
        );
        throwIfTriggered(evaluateRateLimit(routeMatch, fingerprint, null, identities, 1.0D));
    }

    public void enforceAuthenticatedRequest(HttpServletRequest request, Long userId) {
        if (!antiCrawlerProperties.isEnabled()) {
            return;
        }
        AntiCrawlerRouteMatch routeMatch = antiCrawlerRouteClassifier.match(request.getRequestURI(), request.getMethod());
        if (routeMatch == null || routeMatch.group() == AntiCrawlerRouteGroup.AUTH_LOGIN) {
            return;
        }
        RequestFingerprint fingerprint = RequestFingerprintHolder.get();
        if (isWhitelistRequest(fingerprint)) {
            return;
        }

        throwIfTriggered(resolveScopedAction(userId, routeMatch, fingerprint));
        throwIfTriggered(handleMultiIpRisk(userId, routeMatch, fingerprint));

        double thresholdFactor = 1.0D;
        if (isSuspiciousFingerprint(fingerprint)) {
            thresholdFactor = antiCrawlerProperties.getBehavior().getSuspiciousThresholdFactor();
            recordObserveEvent(
                    userId,
                    fingerprint,
                    RULE_CODE_SUSPICIOUS_FINGERPRINT,
                    35,
                    Map.of(
                            "path", routeMatch.normalizedPath(),
                            "missingDeviceId", fingerprint == null || !fingerprint.hasDeviceId(),
                            "wechatUserAgentMatched", fingerprint != null && fingerprint.matchesWechatClient()
                    )
            );
        }

        List<RateIdentity> identities = List.of(
                new RateIdentity("user-device-route", sanitizeForKey("user:" + userId
                        + ":device:" + valueOrDefault(resolveDeviceId(fingerprint), "missing")
                        + ":route:" + routeMatch.normalizedPath())),
                new RateIdentity("ip-route", sanitizeForKey("ip:" + valueOrDefault(fingerprint == null ? null : fingerprint.clientIp(), "unknown")
                        + ":route:" + routeMatch.normalizedPath()))
        );

        throwIfTriggered(evaluateRateLimit(routeMatch, fingerprint, userId, identities, thresholdFactor));
        throwIfTriggered(handlePracticeSessionTraversalRisk(userId, routeMatch, request, fingerprint));
    }

    public void inspectPracticeSubmitBehavior(Long userId, Integer usedSeconds) {
        if (!antiCrawlerProperties.isEnabled()) {
            return;
        }
        RequestFingerprint fingerprint = RequestFingerprintHolder.get();
        if (isWhitelistRequest(fingerprint)) {
            return;
        }
        AntiCrawlerRouteMatch routeMatch = fingerprint == null
                ? null
                : antiCrawlerRouteClassifier.match(fingerprint.path(), fingerprint.method());
        if (routeMatch == null || routeMatch.group() != AntiCrawlerRouteGroup.PRACTICE_SUBMIT) {
            return;
        }
        try {
            String identity = sanitizeForKey("user:" + userId
                    + ":device:" + valueOrDefault(resolveDeviceId(fingerprint), "missing")
                    + ":route:" + routeMatch.normalizedPath());
            AntiCrawlerProperties.GroupRuleProperties groupRuleProperties = antiCrawlerProperties.requireGroup(routeMatch.group());
            AntiCrawlerProperties.BehaviorRuleProperties behaviorRuleProperties = antiCrawlerProperties.getBehavior();

            AntiCrawlerStateStore.ExpiringValue activeBlock = antiCrawlerStateStore.getExpiringValue(actionKey("block", routeMatch.group(), identity));
            if (activeBlock != null) {
                throwIfTriggered(AntiCrawlerDecision.blocked(activeBlock.value(), activeBlock.ttlSeconds(), 95));
                return;
            }

            long totalSubmitCount = antiCrawlerStateStore.incrementCounter(
                    bucketCounterKey("robot-submit-total", identity, behaviorRuleProperties.getRobotWindowSeconds()),
                    Duration.ofSeconds(behaviorRuleProperties.getRobotWindowSeconds() * 2)
            );
            long fastSubmitCount = usedSeconds != null && usedSeconds <= behaviorRuleProperties.getRobotFastUsedSeconds()
                    ? antiCrawlerStateStore.incrementCounter(
                    bucketCounterKey("robot-submit-fast", identity, behaviorRuleProperties.getRobotWindowSeconds()),
                    Duration.ofSeconds(behaviorRuleProperties.getRobotWindowSeconds() * 2)
            )
                    : getCurrentCounterValueFallback("robot-submit-fast", identity, behaviorRuleProperties.getRobotWindowSeconds());

            if (totalSubmitCount >= behaviorRuleProperties.getRobotSubmitThreshold()
                    && fastSubmitCount * 100 >= totalSubmitCount * behaviorRuleProperties.getRobotFastRatioPercent()) {
                antiCrawlerStateStore.setExpiringValue(
                        actionKey("block", routeMatch.group(), identity),
                        RULE_CODE_BOT_SUBMIT,
                        Duration.ofSeconds(groupRuleProperties.getBlockSeconds())
                );
                AntiCrawlerDecision decision = AntiCrawlerDecision.blocked(RULE_CODE_BOT_SUBMIT, groupRuleProperties.getBlockSeconds(), 95);
                antiCrawlerRiskEventService.recordEvent(
                        userId,
                        fingerprint,
                        RULE_CODE_BOT_SUBMIT,
                        AntiCrawlerAction.BLOCKED,
                        decision.riskScore(),
                        buildPayload(
                                routeMatch,
                                "user-device-route",
                                identity,
                                Map.of(
                                        "usedSeconds", usedSeconds,
                                        "totalSubmitCount", totalSubmitCount,
                                        "fastSubmitCount", fastSubmitCount,
                                        "fastRatioPercent", totalSubmitCount == 0 ? 0 : fastSubmitCount * 100 / totalSubmitCount
                                )
                        )
                );
                throwViolation(decision);
            }
        } catch (AntiCrawlerViolationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("anti-crawler downgraded to observe-only while checking practice submit", ex);
            recordObserveEvent(
                    userId,
                    fingerprint,
                    RULE_CODE_REDIS_OBSERVE_ONLY,
                    20,
                    Map.of("reason", "practice-submit-risk-store-failed")
            );
        }
    }

    private AntiCrawlerDecision resolveScopedAction(Long userId,
                                                    AntiCrawlerRouteMatch routeMatch,
                                                    RequestFingerprint fingerprint) {
        try {
            AntiCrawlerStateStore.ExpiringValue activeBlock = antiCrawlerStateStore.getExpiringValue(scopeActionKey("all", userId));
            if (activeBlock != null) {
                return AntiCrawlerDecision.blocked(activeBlock.value(), activeBlock.ttlSeconds(), 90);
            }
            if (routeMatch.readOperation()) {
                AntiCrawlerStateStore.ExpiringValue readCooldown = antiCrawlerStateStore.getExpiringValue(scopeActionKey("read", userId));
                if (readCooldown != null) {
                    return AntiCrawlerDecision.cooldown(readCooldown.value(), readCooldown.ttlSeconds(), 80);
                }
            }
            return AntiCrawlerDecision.allow();
        } catch (Exception ex) {
            log.warn("anti-crawler downgraded to observe-only while checking scoped action", ex);
            recordObserveEvent(userId, fingerprint, RULE_CODE_REDIS_OBSERVE_ONLY, 20, Map.of("reason", "scope-action-store-failed"));
            return AntiCrawlerDecision.allow();
        }
    }

    private AntiCrawlerDecision handleMultiIpRisk(Long userId,
                                                  AntiCrawlerRouteMatch routeMatch,
                                                  RequestFingerprint fingerprint) {
        try {
            AntiCrawlerProperties.BehaviorRuleProperties behaviorRuleProperties = antiCrawlerProperties.getBehavior();
            String ipIdentity = sanitizeForKey("user:" + userId);
            long distinctIpCount = antiCrawlerStateStore.addSetMemberAndCount(
                    setKey("multi-ip", ipIdentity),
                    valueOrDefault(fingerprint == null ? null : fingerprint.clientIp(), "unknown"),
                    Duration.ofSeconds(behaviorRuleProperties.getMultiIpWindowSeconds())
            );
            if (distinctIpCount < behaviorRuleProperties.getMultiIpDistinctThreshold()) {
                return AntiCrawlerDecision.allow();
            }

            AntiCrawlerStateStore.ExpiringValue scopeAllBlock = antiCrawlerStateStore.getExpiringValue(scopeActionKey("all", userId));
            if (scopeAllBlock != null) {
                return AntiCrawlerDecision.blocked(scopeAllBlock.value(), scopeAllBlock.ttlSeconds(), 90);
            }
            AntiCrawlerStateStore.ExpiringValue activeReadCooldown = antiCrawlerStateStore.getExpiringValue(scopeActionKey("read", userId));
            if (activeReadCooldown != null) {
                if (routeMatch.readOperation()) {
                    return AntiCrawlerDecision.cooldown(activeReadCooldown.value(), activeReadCooldown.ttlSeconds(), 80);
                }
                return AntiCrawlerDecision.allow();
            }

            long incidentCount = antiCrawlerStateStore.incrementCounter(
                    bucketCounterKey("multi-ip-incident", ipIdentity, behaviorRuleProperties.getMultiIpRepeatWindowSeconds()),
                    Duration.ofSeconds(behaviorRuleProperties.getMultiIpRepeatWindowSeconds() * 2)
            );
            Map<String, Object> payload = buildPayload(
                    routeMatch,
                    "user",
                    ipIdentity,
                    Map.of(
                            "distinctIpCount", distinctIpCount,
                            "incidentCount", incidentCount
                    )
            );
            if (incidentCount >= 2) {
                antiCrawlerStateStore.setExpiringValue(
                        scopeActionKey("all", userId),
                        RULE_CODE_MULTI_IP_SWITCH,
                        Duration.ofSeconds(behaviorRuleProperties.getMultiIpBlockSeconds())
                );
                AntiCrawlerDecision decision = AntiCrawlerDecision.blocked(
                        RULE_CODE_MULTI_IP_SWITCH,
                        behaviorRuleProperties.getMultiIpBlockSeconds(),
                        90
                );
                antiCrawlerRiskEventService.recordEvent(userId, fingerprint, RULE_CODE_MULTI_IP_SWITCH, AntiCrawlerAction.BLOCKED, decision.riskScore(), payload);
                return decision;
            }

            antiCrawlerStateStore.setExpiringValue(
                    scopeActionKey("read", userId),
                    RULE_CODE_MULTI_IP_SWITCH,
                    Duration.ofSeconds(behaviorRuleProperties.getMultiIpCooldownSeconds())
            );
            AntiCrawlerDecision decision = AntiCrawlerDecision.cooldown(
                    RULE_CODE_MULTI_IP_SWITCH,
                    behaviorRuleProperties.getMultiIpCooldownSeconds(),
                    80
            );
            antiCrawlerRiskEventService.recordEvent(userId, fingerprint, RULE_CODE_MULTI_IP_SWITCH, AntiCrawlerAction.COOLDOWN, decision.riskScore(), payload);
            return routeMatch.readOperation() ? decision : AntiCrawlerDecision.allow();
        } catch (Exception ex) {
            log.warn("anti-crawler downgraded to observe-only while checking multi-ip risk", ex);
            recordObserveEvent(userId, fingerprint, RULE_CODE_REDIS_OBSERVE_ONLY, 20, Map.of("reason", "multi-ip-store-failed"));
            return AntiCrawlerDecision.allow();
        }
    }

    private AntiCrawlerDecision handlePracticeSessionTraversalRisk(Long userId,
                                                                   AntiCrawlerRouteMatch routeMatch,
                                                                   HttpServletRequest request,
                                                                   RequestFingerprint fingerprint) {
        if (routeMatch.group() != AntiCrawlerRouteGroup.PRACTICE_SESSION_READ) {
            return AntiCrawlerDecision.allow();
        }
        try {
            AntiCrawlerProperties.BehaviorRuleProperties behaviorRuleProperties = antiCrawlerProperties.getBehavior();
            AntiCrawlerProperties.GroupRuleProperties groupRuleProperties = antiCrawlerProperties.requireGroup(routeMatch.group());
            String identity = sanitizeForKey("user:" + userId + ":device:" + valueOrDefault(resolveDeviceId(fingerprint), "missing")
                    + ":route:" + routeMatch.normalizedPath());

            String questionId = resolvePracticeQuestionId(request);
            if (questionId != null) {
                long distinctQuestionCount = antiCrawlerStateStore.addSetMemberAndCount(
                        setKey("practice-session-question", identity),
                        questionId,
                        Duration.ofSeconds(behaviorRuleProperties.getScanQuestionWindowSeconds())
                );
                if (distinctQuestionCount >= behaviorRuleProperties.getScanQuestionDistinctThreshold()) {
                    antiCrawlerStateStore.setExpiringValue(
                            actionKey("block", routeMatch.group(), identity),
                            RULE_CODE_SESSION_QUESTION_SCAN,
                            Duration.ofSeconds(groupRuleProperties.getBlockSeconds())
                    );
                    AntiCrawlerDecision decision = AntiCrawlerDecision.blocked(
                            RULE_CODE_SESSION_QUESTION_SCAN,
                            groupRuleProperties.getBlockSeconds(),
                            95
                    );
                    antiCrawlerRiskEventService.recordEvent(
                            userId,
                            fingerprint,
                            RULE_CODE_SESSION_QUESTION_SCAN,
                            AntiCrawlerAction.BLOCKED,
                            decision.riskScore(),
                            buildPayload(
                                    routeMatch,
                                    "user-device-route",
                                    identity,
                                    Map.of("distinctQuestionCount", distinctQuestionCount, "questionId", questionId)
                            )
                    );
                    return decision;
                }
            }

            String bankId = trimToNull(request.getParameter("bankId"));
            if (bankId != null) {
                long distinctBankCount = antiCrawlerStateStore.addSetMemberAndCount(
                        setKey("practice-session-bank", identity),
                        bankId,
                        Duration.ofSeconds(behaviorRuleProperties.getScanBankWindowSeconds())
                );
                if (distinctBankCount >= behaviorRuleProperties.getScanBankDistinctThreshold()) {
                    antiCrawlerStateStore.setExpiringValue(
                            actionKey("block", routeMatch.group(), identity),
                            RULE_CODE_SESSION_BANK_SCAN,
                            Duration.ofSeconds(groupRuleProperties.getBlockSeconds())
                    );
                    AntiCrawlerDecision decision = AntiCrawlerDecision.blocked(
                            RULE_CODE_SESSION_BANK_SCAN,
                            groupRuleProperties.getBlockSeconds(),
                            95
                    );
                    antiCrawlerRiskEventService.recordEvent(
                            userId,
                            fingerprint,
                            RULE_CODE_SESSION_BANK_SCAN,
                            AntiCrawlerAction.BLOCKED,
                            decision.riskScore(),
                            buildPayload(
                                    routeMatch,
                                    "user-device-route",
                                    identity,
                                    Map.of("distinctBankCount", distinctBankCount, "bankId", bankId)
                            )
                    );
                    return decision;
                }
            }
            return AntiCrawlerDecision.allow();
        } catch (Exception ex) {
            log.warn("anti-crawler downgraded to observe-only while checking practice session traversal", ex);
            recordObserveEvent(userId, fingerprint, RULE_CODE_REDIS_OBSERVE_ONLY, 20, Map.of("reason", "practice-session-risk-store-failed"));
            return AntiCrawlerDecision.allow();
        }
    }

    private AntiCrawlerDecision evaluateRateLimit(AntiCrawlerRouteMatch routeMatch,
                                                  RequestFingerprint fingerprint,
                                                  Long userId,
                                                  List<RateIdentity> identities,
                                                  double thresholdFactor) {
        AntiCrawlerDecision strongestDecision = AntiCrawlerDecision.allow();
        for (RateIdentity identity : identities) {
            AntiCrawlerDecision decision = evaluateSingleIdentityRateLimit(routeMatch, fingerprint, userId, identity, thresholdFactor);
            if (decision.strongerThan(strongestDecision)) {
                strongestDecision = decision;
            }
        }
        return strongestDecision;
    }

    private AntiCrawlerDecision evaluateSingleIdentityRateLimit(AntiCrawlerRouteMatch routeMatch,
                                                                RequestFingerprint fingerprint,
                                                                Long userId,
                                                                RateIdentity identity,
                                                                double thresholdFactor) {
        try {
            AntiCrawlerProperties.GroupRuleProperties groupRuleProperties = antiCrawlerProperties.requireGroup(routeMatch.group());
            String blockKey = actionKey("block", routeMatch.group(), identity.key());
            AntiCrawlerStateStore.ExpiringValue activeBlock = antiCrawlerStateStore.getExpiringValue(blockKey);
            if (activeBlock != null) {
                return AntiCrawlerDecision.blocked(activeBlock.value(), activeBlock.ttlSeconds(), 90);
            }

            WindowCount oneSecond = incrementWindowCount(routeMatch, identity.key(), groupRuleProperties.getOneSecondLimit(), thresholdFactor, 1);
            WindowCount oneMinute = incrementWindowCount(routeMatch, identity.key(), groupRuleProperties.getOneMinuteLimit(), thresholdFactor, 60);
            WindowCount tenMinute = incrementWindowCount(routeMatch, identity.key(), groupRuleProperties.getTenMinuteLimit(), thresholdFactor, 600);
            boolean exceeded = oneSecond.exceeded() || oneMinute.exceeded() || tenMinute.exceeded();

            if (exceeded) {
                long exceedCount = antiCrawlerStateStore.incrementCounter(
                        bucketCounterKey("rate-exceed:" + routeMatch.group().code(), identity.key(), groupRuleProperties.getEscalationWindowSeconds()),
                        Duration.ofSeconds(groupRuleProperties.getEscalationWindowSeconds() * 2)
                );
                Map<String, Object> payload = buildPayload(
                        routeMatch,
                        identity.dimension(),
                        identity.key(),
                        buildWindowPayload(oneSecond, oneMinute, tenMinute, thresholdFactor, exceedCount)
                );
                boolean shouldNightBlock = routeMatch.readOperation() && reachesNightEscalation(oneSecond, oneMinute, tenMinute);
                if (shouldNightBlock || exceedCount >= groupRuleProperties.getEscalationThreshold()) {
                    antiCrawlerStateStore.setExpiringValue(
                            blockKey,
                            rateLimitRuleCode(routeMatch),
                            Duration.ofSeconds(groupRuleProperties.getBlockSeconds())
                    );
                    AntiCrawlerDecision decision = AntiCrawlerDecision.blocked(rateLimitRuleCode(routeMatch), groupRuleProperties.getBlockSeconds(), 85);
                    antiCrawlerRiskEventService.recordEvent(userId, fingerprint, rateLimitRuleCode(routeMatch), AntiCrawlerAction.BLOCKED, decision.riskScore(), payload);
                    return decision;
                }
                antiCrawlerStateStore.setExpiringValue(
                        actionKey("cooldown", routeMatch.group(), identity.key()),
                        rateLimitRuleCode(routeMatch),
                        Duration.ofSeconds(groupRuleProperties.getCooldownSeconds())
                );
                AntiCrawlerDecision decision = AntiCrawlerDecision.cooldown(rateLimitRuleCode(routeMatch), groupRuleProperties.getCooldownSeconds(), 70);
                antiCrawlerRiskEventService.recordEvent(userId, fingerprint, rateLimitRuleCode(routeMatch), AntiCrawlerAction.COOLDOWN, decision.riskScore(), payload);
                return decision;
            }

            AntiCrawlerStateStore.ExpiringValue activeCooldown = antiCrawlerStateStore.getExpiringValue(actionKey("cooldown", routeMatch.group(), identity.key()));
            if (activeCooldown != null) {
                return AntiCrawlerDecision.cooldown(activeCooldown.value(), activeCooldown.ttlSeconds(), 70);
            }
            return AntiCrawlerDecision.allow();
        } catch (Exception ex) {
            log.warn("anti-crawler downgraded to observe-only while evaluating rate limit, group={}, identity={}",
                    routeMatch.group().code(), identity.key(), ex);
            recordObserveEvent(
                    userId,
                    fingerprint,
                    RULE_CODE_REDIS_OBSERVE_ONLY,
                    20,
                    Map.of(
                            "reason", "rate-limit-store-failed",
                            "group", routeMatch.group().code(),
                            "identityDimension", identity.dimension()
                    )
            );
            return AntiCrawlerDecision.allow();
        }
    }

    private WindowCount incrementWindowCount(AntiCrawlerRouteMatch routeMatch,
                                             String identityKey,
                                             long limit,
                                             double thresholdFactor,
                                             long windowSeconds) {
        long resolvedLimit = applyThresholdFactor(limit, thresholdFactor);
        if (resolvedLimit <= 0) {
            return WindowCount.disabled(windowSeconds);
        }
        long count = antiCrawlerStateStore.incrementCounter(
                bucketCounterKey("rate:" + routeMatch.group().code(), identityKey, windowSeconds),
                Duration.ofSeconds(windowSeconds * 2)
        );
        return new WindowCount(windowSeconds, count, resolvedLimit);
    }

    private Map<String, Object> buildWindowPayload(WindowCount oneSecond,
                                                   WindowCount oneMinute,
                                                   WindowCount tenMinute,
                                                   double thresholdFactor,
                                                   long exceedCount) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("oneSecondCount", oneSecond.count());
        payload.put("oneSecondLimit", oneSecond.limit());
        payload.put("oneMinuteCount", oneMinute.count());
        payload.put("oneMinuteLimit", oneMinute.limit());
        payload.put("tenMinuteCount", tenMinute.count());
        payload.put("tenMinuteLimit", tenMinute.limit());
        payload.put("thresholdFactor", thresholdFactor);
        payload.put("exceedCount", exceedCount);
        payload.put("nightWindow", isNightWindow());
        return payload;
    }

    private boolean reachesNightEscalation(WindowCount oneSecond, WindowCount oneMinute, WindowCount tenMinute) {
        if (!isNightWindow()) {
            return false;
        }
        double multiplier = antiCrawlerProperties.getBehavior().getNightReadMultiplier();
        return reachesNightEscalation(oneSecond, multiplier)
                || reachesNightEscalation(oneMinute, multiplier)
                || reachesNightEscalation(tenMinute, multiplier);
    }

    private boolean reachesNightEscalation(WindowCount windowCount, double multiplier) {
        return windowCount.limit() > 0 && windowCount.count() >= Math.ceil(windowCount.limit() * multiplier);
    }

    private boolean isNightWindow() {
        LocalTime now = LocalTime.now(antiCrawlerClock);
        int startHour = antiCrawlerProperties.getBehavior().getNightReadStartHour();
        int endHour = antiCrawlerProperties.getBehavior().getNightReadEndHour();
        return now.getHour() >= startHour && now.getHour() < endHour;
    }

    private boolean isSuspiciousFingerprint(RequestFingerprint fingerprint) {
        return fingerprint == null || !fingerprint.hasDeviceId() || !fingerprint.matchesWechatClient();
    }

    private void throwIfTriggered(AntiCrawlerDecision decision) {
        if (decision != null && decision.triggered()) {
            throwViolation(decision);
        }
    }

    private void throwViolation(AntiCrawlerDecision decision) {
        throw new AntiCrawlerViolationException(
                decision.message(),
                decision.httpStatus(),
                decision.responseCode(),
                decision.toPayload()
        );
    }

    private void recordObserveEvent(Long userId,
                                    RequestFingerprint fingerprint,
                                    String ruleCode,
                                    int riskScore,
                                    Map<String, Object> payload) {
        antiCrawlerRiskEventService.recordEvent(userId, fingerprint, ruleCode, AntiCrawlerAction.OBSERVE, riskScore, payload);
    }

    private boolean isWhitelistRequest(RequestFingerprint fingerprint) {
        if (fingerprint == null || !StringUtils.hasText(fingerprint.clientIp())) {
            return false;
        }
        List<String> whitelistIps = antiCrawlerProperties.getWhitelistIps();
        return whitelistIps != null && whitelistIps.contains(fingerprint.clientIp());
    }

    private String scopeActionKey(String scope, Long userId) {
        return "anti:action:scope:" + scope + ":user:" + userId;
    }

    private String actionKey(String action, AntiCrawlerRouteGroup group, String identity) {
        return "anti:action:" + action + ":" + group.code() + ":" + identity;
    }

    private String setKey(String category, String identity) {
        return "anti:set:" + category + ":" + identity;
    }

    private String bucketCounterKey(String category, String identity, long windowSeconds) {
        long bucket = Instant.now(antiCrawlerClock).getEpochSecond() / windowSeconds;
        return "anti:count:" + category + ":" + identity + ":" + windowSeconds + ":" + bucket;
    }

    private long applyThresholdFactor(long limit, double thresholdFactor) {
        if (limit <= 0) {
            return 0;
        }
        if (thresholdFactor >= 1.0D) {
            return limit;
        }
        return Math.max(1L, (long) Math.floor(limit * thresholdFactor));
    }

    private String rateLimitRuleCode(AntiCrawlerRouteMatch routeMatch) {
        return routeMatch.group().code() + "-rate-limit";
    }

    private String sanitizeForKey(String value) {
        return value.replaceAll("[^a-zA-Z0-9:_{}-]", "_");
    }

    private String valueOrDefault(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String resolveDeviceId(RequestFingerprint fingerprint) {
        if (fingerprint == null || !StringUtils.hasText(fingerprint.deviceId())) {
            return null;
        }
        return fingerprint.deviceId().trim();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String resolvePracticeQuestionId(HttpServletRequest request) {
        String questionId = trimToNull(request.getParameter("questionId"));
        if (questionId != null) {
            return questionId;
        }
        Matcher matcher = PRACTICE_QUESTION_PATH.matcher(request.getRequestURI());
        return matcher.matches() ? matcher.group(1) : null;
    }

    private Map<String, Object> buildPayload(AntiCrawlerRouteMatch routeMatch,
                                             String identityDimension,
                                             String identityKey,
                                             Map<String, Object> detailPayload) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("group", routeMatch.group().code());
        payload.put("path", routeMatch.normalizedPath());
        payload.put("identityDimension", identityDimension);
        payload.put("identityKey", identityKey);
        if (detailPayload != null && !detailPayload.isEmpty()) {
            payload.putAll(detailPayload);
        }
        return payload;
    }

    private long getCurrentCounterValueFallback(String category, String identity, long windowSeconds) {
        try {
            AntiCrawlerStateStore.ExpiringValue currentValue = antiCrawlerStateStore.getExpiringValue(
                    bucketCounterKey(category, identity, windowSeconds)
            );
            if (currentValue == null || !StringUtils.hasText(currentValue.value())) {
                return 0L;
            }
            return Long.parseLong(currentValue.value());
        } catch (Exception ex) {
            return 0L;
        }
    }

    private record RateIdentity(String dimension, String key) {
    }

    private record WindowCount(long windowSeconds, long count, long limit) {

        static WindowCount disabled(long windowSeconds) {
            return new WindowCount(windowSeconds, 0L, 0L);
        }

        boolean exceeded() {
            return limit > 0 && count > limit;
        }
    }
}
