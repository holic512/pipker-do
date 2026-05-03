package org.example.backend.biz.kyyy.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.biz.kyyy.dto.KyyyHomeDailyWordResponse;
import org.example.backend.biz.kyyy.entity.KyyyWord;
import org.example.backend.biz.kyyy.mapper.KyyyWordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * AI 索引: KYYY 用户侧首页每日一词服务。
 */
@Service
public class KyyyHomeDailyWordService {

    private static final Logger log = LoggerFactory.getLogger(KyyyHomeDailyWordService.class);
    private static final String DAILY_WORD_CACHE_KEY = "kyyy:home:daily-words:v1";
    private static final int DAILY_WORD_LIMIT = 3;
    private static final Duration DAILY_WORD_CACHE_TTL = Duration.ofHours(12);
    private static final int MAX_PART_OF_SPEECH_LENGTH = 12;
    private static final int MAX_MEANING_LENGTH = 18;
    private static final TypeReference<List<KyyyHomeDailyWordResponse>> DAILY_WORD_LIST_TYPE = new TypeReference<>() {
    };

    private final KyyyWordMapper kyyyWordMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public KyyyHomeDailyWordService(KyyyWordMapper kyyyWordMapper,
                                    StringRedisTemplate stringRedisTemplate,
                                    ObjectMapper objectMapper) {
        this.kyyyWordMapper = kyyyWordMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<KyyyHomeDailyWordResponse> getDailyWords() {
        List<KyyyHomeDailyWordResponse> cachedWords = readCachedDailyWords();
        if (!cachedWords.isEmpty()) {
            return cachedWords;
        }

        List<KyyyHomeDailyWordResponse> freshWords = loadRandomActiveWords();
        if (!freshWords.isEmpty()) {
            cacheDailyWordsQuietly(freshWords);
        }
        return freshWords;
    }

    private List<KyyyHomeDailyWordResponse> readCachedDailyWords() {
        try {
            String cachedJson = stringRedisTemplate.opsForValue().get(DAILY_WORD_CACHE_KEY);
            if (!StringUtils.hasText(cachedJson)) {
                return List.of();
            }
            List<KyyyHomeDailyWordResponse> cachedWords = objectMapper.readValue(cachedJson, DAILY_WORD_LIST_TYPE);
            if (cachedWords == null || cachedWords.isEmpty()) {
                return List.of();
            }
            return cachedWords.stream()
                    .filter(Objects::nonNull)
                    .filter(item -> StringUtils.hasText(item.getWordText()))
                    .limit(DAILY_WORD_LIMIT)
                    .toList();
        } catch (Exception error) {
            log.warn("Failed to read cached KYYY daily words", error);
            return List.of();
        }
    }

    private List<KyyyHomeDailyWordResponse> loadRandomActiveWords() {
        QueryWrapper<KyyyWord> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(
                "id",
                "word_text",
                "part_of_speech",
                "meaning_cn"
        );
        queryWrapper.eq("status", 1);
        queryWrapper.last("order by rand() limit " + DAILY_WORD_LIMIT);

        List<KyyyWord> words = kyyyWordMapper.selectList(queryWrapper);
        if (words == null || words.isEmpty()) {
            return List.of();
        }
        return words.stream()
                .map(this::toDailyWordResponse)
                .filter(item -> StringUtils.hasText(item.getWordText()))
                .toList();
    }

    private KyyyHomeDailyWordResponse toDailyWordResponse(KyyyWord word) {
        String partOfSpeech = normalizeShortText(word.getPartOfSpeech(), MAX_PART_OF_SPEECH_LENGTH);
        return new KyyyHomeDailyWordResponse(
                word.getId(),
                normalizeText(word.getWordText()),
                partOfSpeech,
                summarizeMeaning(word.getMeaningCn(), partOfSpeech)
        );
    }

    private String summarizeMeaning(String meaningCn, String partOfSpeech) {
        String normalized = firstMeaningSegment(meaningCn);
        if (!StringUtils.hasText(normalized)) {
            return "";
        }
        String withoutOrder = normalized.replaceFirst("^\\d+[.、]\\s*", "").trim();
        if (StringUtils.hasText(partOfSpeech) && withoutOrder.startsWith(partOfSpeech)) {
            withoutOrder = withoutOrder.substring(partOfSpeech.length()).trim();
        }
        return normalizeShortText(withoutOrder, MAX_MEANING_LENGTH);
    }

    private String firstMeaningSegment(String value) {
        String normalized = normalizeText(value);
        if (!StringUtils.hasText(normalized)) {
            return "";
        }
        String firstLine = firstNonBlank(normalized.split("\\n+"));
        if (!StringUtils.hasText(firstLine)) {
            return "";
        }
        String[] numberedSegments = firstLine.split("(?=\\d+[.、]\\s*)");
        for (String segment : numberedSegments) {
            if (StringUtils.hasText(segment)) {
                String first = segment.split("[；;]", 2)[0].trim();
                if (StringUtils.hasText(first)) {
                    return first;
                }
            }
        }
        return firstLine.split("[；;]", 2)[0].trim();
    }

    private String firstNonBlank(String[] values) {
        if (values == null || values.length == 0) {
            return "";
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private String normalizeShortText(String value, int limit) {
        String normalized = normalizeText(value);
        if (!StringUtils.hasText(normalized)) {
            return "";
        }
        if (normalized.length() <= limit) {
            return normalized;
        }
        int preferredCut = normalized.lastIndexOf(' ', Math.max(limit - 1, 0));
        if (preferredCut >= Math.max(limit / 2, 4)) {
            return normalized.substring(0, preferredCut).trim() + "…";
        }
        return normalized.substring(0, Math.max(limit - 1, 0)).trim() + "…";
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private void cacheDailyWordsQuietly(List<KyyyHomeDailyWordResponse> words) {
        try {
            stringRedisTemplate.opsForValue().set(
                    DAILY_WORD_CACHE_KEY,
                    objectMapper.writeValueAsString(words),
                    DAILY_WORD_CACHE_TTL
            );
        } catch (Exception error) {
            log.warn("Failed to cache KYYY daily words", error);
        }
    }
}
