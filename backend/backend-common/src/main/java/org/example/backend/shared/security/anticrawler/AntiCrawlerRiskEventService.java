package org.example.backend.shared.security.anticrawler;

import org.example.backend.shared.security.entity.AppSecurityRiskEvent;
import org.example.backend.shared.security.mapper.AppSecurityRiskEventMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;

/**
 * AI 索引: 反扒风险事件落库服务。
 */
@Service
public class AntiCrawlerRiskEventService {

    private static final Logger log = LoggerFactory.getLogger(AntiCrawlerRiskEventService.class);

    private final AppSecurityRiskEventMapper appSecurityRiskEventMapper;

    public AntiCrawlerRiskEventService(AppSecurityRiskEventMapper appSecurityRiskEventMapper) {
        this.appSecurityRiskEventMapper = appSecurityRiskEventMapper;
    }

    public void recordEvent(Long userId,
                            RequestFingerprint fingerprint,
                            String ruleCode,
                            AntiCrawlerAction action,
                            int riskScore,
                            Map<String, Object> payload) {
        try {
            AppSecurityRiskEvent event = new AppSecurityRiskEvent();
            event.setUserId(userId);
            event.setDeviceId(fingerprint == null ? null : fingerprint.deviceId());
            event.setIp(fingerprint == null ? null : fingerprint.clientIp());
            event.setPath(fingerprint == null ? null : fingerprint.path());
            event.setMethod(fingerprint == null ? null : fingerprint.method());
            event.setRuleCode(ruleCode);
            event.setAction(action == null ? null : action.value());
            event.setRiskScore(riskScore);
            event.setRequestId(fingerprint == null ? null : fingerprint.requestId());
            event.setTokenHash(fingerprint == null ? null : fingerprint.tokenHash());
            event.setPayloadJson(toPayloadJson(payload));
            event.setCreatedAt(LocalDateTime.now());
            appSecurityRiskEventMapper.insert(event);
        } catch (Exception ex) {
            log.warn("failed to persist anti-crawler event, ruleCode={}", ruleCode, ex);
        }
    }

    private String toPayloadJson(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return null;
        }
        return toJson(payload);
    }

    @SuppressWarnings("unchecked")
    private String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof String item) {
            return "\"" + escapeJson(item) + "\"";
        }
        if (value instanceof Map<?, ?> item) {
            StringBuilder builder = new StringBuilder("{");
            Iterator<? extends Map.Entry<?, ?>> iterator = item.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<?, ?> entry = iterator.next();
                builder.append("\"").append(escapeJson(String.valueOf(entry.getKey()))).append("\":");
                builder.append(toJson(entry.getValue()));
                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
            builder.append("}");
            return builder.toString();
        }
        if (value instanceof Iterable<?> item) {
            StringBuilder builder = new StringBuilder("[");
            Iterator<?> iterator = item.iterator();
            while (iterator.hasNext()) {
                builder.append(toJson(iterator.next()));
                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
            builder.append("]");
            return builder.toString();
        }
        if (value.getClass().isArray()) {
            Object[] items = (Object[]) value;
            StringBuilder builder = new StringBuilder("[");
            for (int index = 0; index < items.length; index++) {
                builder.append(toJson(items[index]));
                if (index + 1 < items.length) {
                    builder.append(",");
                }
            }
            builder.append("]");
            return builder.toString();
        }
        return "\"" + escapeJson(String.valueOf(value)) + "\"";
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
