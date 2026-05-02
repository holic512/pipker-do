package org.example.backend.biz.kyyy.support;

import org.example.backend.biz.kyyy.dto.KyyyPracticeSettingOptionResponse;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;

import java.util.List;
import java.util.Locale;

/**
 * AI 索引: KYYY 英语一/英语二考试方向枚举辅助。
 */
public final class KyyyExamDirectionSupport {

    public static final String ENGLISH_ONE = "english_one";
    public static final String ENGLISH_TWO = "english_two";
    public static final String DEFAULT_DIRECTION = ENGLISH_ONE;

    private KyyyExamDirectionSupport() {
    }

    public static String normalizeOrDefault(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_DIRECTION;
        }
        return normalize(value);
    }

    public static String normalize(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replace("-", "_");
        return switch (normalized) {
            case ENGLISH_ONE, "one", "english1", "english_i", "english_1", "英一", "英语一" -> ENGLISH_ONE;
            case ENGLISH_TWO, "two", "english2", "english_ii", "english_2", "英二", "英语二" -> ENGLISH_TWO;
            default -> throw new BusinessException(ApiResponseCode.BAD_REQUEST, "考试方向仅支持英一或英二");
        };
    }

    public static String labelOf(String examDirection) {
        String normalized = normalizeOrDefault(examDirection);
        return ENGLISH_TWO.equals(normalized) ? "英二" : "英一";
    }

    public static List<KyyyPracticeSettingOptionResponse> options() {
        return List.of(
                new KyyyPracticeSettingOptionResponse(ENGLISH_ONE, "英一"),
                new KyyyPracticeSettingOptionResponse(ENGLISH_TWO, "英二")
        );
    }
}
