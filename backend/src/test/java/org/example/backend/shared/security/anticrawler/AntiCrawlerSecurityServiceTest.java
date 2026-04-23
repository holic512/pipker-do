package org.example.backend.shared.security.anticrawler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
class AntiCrawlerSecurityServiceTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-04-23T06:00:00Z"), ZoneId.of("Asia/Shanghai"));

    @AfterEach
    void tearDown() {
        RequestFingerprintHolder.clear();
    }

    @Test
    void shouldCooldownThenBlockLoginAfterRepeatedExceed() {
        AntiCrawlerSecurityService service = newService(createDefaultProperties());

        for (int index = 0; index < 6; index++) {
            MockHttpServletRequest request = buildRequest("POST", "/api/auth/wechat/login", "127.0.0.1", "dev-1");
            setFingerprint(request);
            service.enforceLoginRequest(request);
        }

        MockHttpServletRequest cooldownRequest = buildRequest("POST", "/api/auth/wechat/login", "127.0.0.1", "dev-1");
        setFingerprint(cooldownRequest);
        AntiCrawlerViolationException cooldown = assertThrows(AntiCrawlerViolationException.class, () -> service.enforceLoginRequest(cooldownRequest));
        assertEquals(429, cooldown.getHttpStatus());
        assertEquals("cooldown", cooldown.getResponsePayload().action());

        MockHttpServletRequest secondExceedRequest = buildRequest("POST", "/api/auth/wechat/login", "127.0.0.1", "dev-1");
        setFingerprint(secondExceedRequest);
        assertThrows(AntiCrawlerViolationException.class, () -> service.enforceLoginRequest(secondExceedRequest));

        MockHttpServletRequest blockRequest = buildRequest("POST", "/api/auth/wechat/login", "127.0.0.1", "dev-1");
        setFingerprint(blockRequest);
        AntiCrawlerViolationException blocked = assertThrows(AntiCrawlerViolationException.class, () -> service.enforceLoginRequest(blockRequest));
        assertEquals(403, blocked.getHttpStatus());
        assertEquals("blocked", blocked.getResponsePayload().action());
        assertEquals("auth-login-rate-limit", blocked.getResponsePayload().ruleCode());
    }

    @Test
    void shouldBlockWhenPracticeSessionScansTooManyQuestions() {
        AntiCrawlerProperties properties = createDefaultProperties();
        properties.getGroups().get(AntiCrawlerRouteGroup.PRACTICE_SESSION_READ.code()).setOneSecondLimit(100);
        properties.getGroups().get(AntiCrawlerRouteGroup.PRACTICE_SESSION_READ.code()).setOneMinuteLimit(1000);
        properties.getGroups().get(AntiCrawlerRouteGroup.PRACTICE_SESSION_READ.code()).setTenMinuteLimit(1000);
        AntiCrawlerSecurityService service = newService(properties);

        for (int questionId = 1; questionId < 40; questionId++) {
            MockHttpServletRequest request = buildRequest("GET", "/api/kyzz/practice/session", "127.0.0.1", "dev-2");
            request.addParameter("questionId", String.valueOf(questionId));
            request.addParameter("bankId", "8");
            setFingerprint(request);
            service.enforceAuthenticatedRequest(request, 9L);
        }

        MockHttpServletRequest blockedRequest = buildRequest("GET", "/api/kyzz/practice/session", "127.0.0.1", "dev-2");
        blockedRequest.addParameter("questionId", "40");
        blockedRequest.addParameter("bankId", "8");
        setFingerprint(blockedRequest);
        AntiCrawlerViolationException blocked = assertThrows(
                AntiCrawlerViolationException.class,
                () -> service.enforceAuthenticatedRequest(blockedRequest, 9L)
        );
        assertEquals(403, blocked.getHttpStatus());
        assertEquals("practice-session-question-scan", blocked.getResponsePayload().ruleCode());
    }

    @Test
    void shouldBlockRobotPracticeSubmitWhenFastRatioIsTooHigh() {
        AntiCrawlerSecurityService service = newService(createDefaultProperties());

        for (int index = 0; index < 19; index++) {
            setFingerprint(buildRequest("POST", "/api/kyzz/practice/questions/11/review", "127.0.0.1", "dev-3"));
            service.inspectPracticeSubmitBehavior(11L, 1);
        }

        setFingerprint(buildRequest("POST", "/api/kyzz/practice/questions/11/review", "127.0.0.1", "dev-3"));
        AntiCrawlerViolationException blocked = assertThrows(
                AntiCrawlerViolationException.class,
                () -> service.inspectPracticeSubmitBehavior(11L, 1)
        );
        assertEquals(403, blocked.getHttpStatus());
        assertEquals("practice-submit-bot", blocked.getResponsePayload().ruleCode());
    }

    @Test
    void shouldCooldownReadInterfacesWhenAccountSwitchesAcrossIps() {
        AntiCrawlerProperties properties = createDefaultProperties();
        properties.getGroups().get(AntiCrawlerRouteGroup.QUESTION_BANK_READ.code()).setOneSecondLimit(100);
        properties.getGroups().get(AntiCrawlerRouteGroup.QUESTION_BANK_READ.code()).setOneMinuteLimit(1000);
        properties.getGroups().get(AntiCrawlerRouteGroup.QUESTION_BANK_READ.code()).setTenMinuteLimit(1000);
        AntiCrawlerSecurityService service = newService(properties);

        for (int index = 1; index < 4; index++) {
            MockHttpServletRequest request = buildRequest("GET", "/api/kyzz/question-banks/public", "10.0.0." + index, "dev-4");
            setFingerprint(request);
            service.enforceAuthenticatedRequest(request, 20L);
        }

        MockHttpServletRequest cooldownRequest = buildRequest("GET", "/api/kyzz/question-banks/public", "10.0.0.4", "dev-4");
        setFingerprint(cooldownRequest);
        AntiCrawlerViolationException cooldown = assertThrows(
                AntiCrawlerViolationException.class,
                () -> service.enforceAuthenticatedRequest(cooldownRequest, 20L)
        );
        assertEquals(429, cooldown.getHttpStatus());
        assertEquals("account-multi-ip-switch", cooldown.getResponsePayload().ruleCode());
    }

    private AntiCrawlerSecurityService newService(AntiCrawlerProperties properties) {
        return new AntiCrawlerSecurityService(
                properties,
                new AntiCrawlerRouteClassifier(),
                new InMemoryAntiCrawlerStateStore(fixedClock),
                new NoOpAntiCrawlerRiskEventService(),
                fixedClock
        );
    }

    private AntiCrawlerProperties createDefaultProperties() {
        AntiCrawlerProperties properties = new AntiCrawlerProperties();
        Map<String, AntiCrawlerProperties.GroupRuleProperties> groups = new LinkedHashMap<>();
        groups.put(AntiCrawlerRouteGroup.AUTH_LOGIN.code(), createGroup(6, 30, 100, 60, 1800));
        groups.put(AntiCrawlerRouteGroup.PROFILE_READWRITE.code(), createGroup(10, 120, 500, 30, 1800));
        groups.put(AntiCrawlerRouteGroup.QUESTION_BANK_READ.code(), createGroup(6, 90, 300, 60, 1800));
        groups.put(AntiCrawlerRouteGroup.PRACTICE_SESSION_READ.code(), createGroup(4, 120, 400, 60, 1800));
        groups.put(AntiCrawlerRouteGroup.PRACTICE_SUBMIT.code(), createGroup(3, 60, 200, 60, 1800));
        groups.put(AntiCrawlerRouteGroup.AVATAR_UPLOAD.code(), createGroup(0, 10, 30, 120, 1800));
        properties.setGroups(groups);
        return properties;
    }

    private AntiCrawlerProperties.GroupRuleProperties createGroup(long oneSecond,
                                                                  long oneMinute,
                                                                  long tenMinute,
                                                                  long cooldownSeconds,
                                                                  long blockSeconds) {
        AntiCrawlerProperties.GroupRuleProperties groupRuleProperties = new AntiCrawlerProperties.GroupRuleProperties();
        groupRuleProperties.setOneSecondLimit(oneSecond);
        groupRuleProperties.setOneMinuteLimit(oneMinute);
        groupRuleProperties.setTenMinuteLimit(tenMinute);
        groupRuleProperties.setCooldownSeconds(cooldownSeconds);
        groupRuleProperties.setBlockSeconds(blockSeconds);
        groupRuleProperties.setEscalationWindowSeconds(600);
        groupRuleProperties.setEscalationThreshold(3);
        return groupRuleProperties;
    }

    private MockHttpServletRequest buildRequest(String method, String path, String ip, String deviceId) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.addHeader("Authorization", "Bearer test-token");
        request.addHeader("User-Agent", "MicroMessenger TestClient");
        request.addHeader(AntiCrawlerHeaderNames.REQUEST_ID, "req-test");
        request.addHeader(AntiCrawlerHeaderNames.DEVICE_ID, deviceId);
        request.addHeader(AntiCrawlerHeaderNames.CLIENT_PLATFORM, "mp-weixin");
        request.setRemoteAddr(ip);
        return request;
    }

    private void setFingerprint(MockHttpServletRequest request) {
        RequestFingerprintHolder.set(new RequestFingerprint(
                request.getHeader(AntiCrawlerHeaderNames.REQUEST_ID),
                request.getRequestURI(),
                request.getMethod(),
                request.getRemoteAddr(),
                request.getHeader(AntiCrawlerHeaderNames.DEVICE_ID),
                request.getHeader("User-Agent"),
                request.getHeader("Referer"),
                request.getHeader(AntiCrawlerHeaderNames.CLIENT_PLATFORM),
                request.getHeader(AntiCrawlerHeaderNames.CLIENT_VERSION),
                "token-hash"
        ));
    }

    private static final class InMemoryAntiCrawlerStateStore implements AntiCrawlerStateStore {

        private final Clock clock;
        private final Map<String, CounterEntry> counters = new HashMap<>();
        private final Map<String, ValueEntry> values = new HashMap<>();
        private final Map<String, SetEntry> sets = new HashMap<>();

        private InMemoryAntiCrawlerStateStore(Clock clock) {
            this.clock = clock;
        }

        @Override
        public long incrementCounter(String key, Duration ttl) {
            CounterEntry entry = counters.get(key);
            if (entry == null || entry.expiresAt <= now()) {
                entry = new CounterEntry(0L, now() + ttl.toMillis());
                counters.put(key, entry);
            }
            entry.count += 1;
            entry.expiresAt = now() + ttl.toMillis();
            return entry.count;
        }

        @Override
        public void setExpiringValue(String key, String value, Duration ttl) {
            values.put(key, new ValueEntry(value, now() + ttl.toMillis()));
        }

        @Override
        public ExpiringValue getExpiringValue(String key) {
            ValueEntry entry = values.get(key);
            if (entry == null || entry.expiresAt <= now()) {
                values.remove(key);
                return null;
            }
            return new ExpiringValue(entry.value, Math.max(1L, (entry.expiresAt - now()) / 1000));
        }

        @Override
        public long addSetMemberAndCount(String key, String member, Duration ttl) {
            SetEntry entry = sets.get(key);
            if (entry == null || entry.expiresAt <= now()) {
                entry = new SetEntry(new HashSet<>(), now() + ttl.toMillis());
                sets.put(key, entry);
            }
            entry.values.add(member);
            entry.expiresAt = now() + ttl.toMillis();
            return entry.values.size();
        }

        private long now() {
            return clock.millis();
        }

        private static final class CounterEntry {
            private long count;
            private long expiresAt;

            private CounterEntry(long count, long expiresAt) {
                this.count = count;
                this.expiresAt = expiresAt;
            }
        }

        private static final class ValueEntry {
            private final String value;
            private final long expiresAt;

            private ValueEntry(String value, long expiresAt) {
                this.value = value;
                this.expiresAt = expiresAt;
            }
        }

        private static final class SetEntry {
            private final Set<String> values;
            private long expiresAt;

            private SetEntry(Set<String> values, long expiresAt) {
                this.values = values;
                this.expiresAt = expiresAt;
            }
        }
    }

    private static final class NoOpAntiCrawlerRiskEventService extends AntiCrawlerRiskEventService {

        private NoOpAntiCrawlerRiskEventService() {
            super(null);
        }

        @Override
        public void recordEvent(Long userId,
                                RequestFingerprint fingerprint,
                                String ruleCode,
                                AntiCrawlerAction action,
                                int riskScore,
                                Map<String, Object> payload) {
            // no-op for unit tests
        }
    }
}
