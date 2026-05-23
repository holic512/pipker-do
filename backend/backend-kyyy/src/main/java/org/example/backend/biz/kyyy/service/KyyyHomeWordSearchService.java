/**
 * @file KyyyHomeWordSearchService
 * @project pipker-do
 * @module 考研英语 / 首页查词
 * @description 为小程序首页提供全局有效单词检索与详情能力。
 * @logic 1. 标准化查询词；2. 在全局有效单词中按精确、前缀、包含顺序查询；3. 按 wordId 拉取详情与多例句。
 * @dependencies Mapper: KyyyWordMapper, Mapper: KyyyWordExampleMapper, DTO: KyyyHomeWordDetailResponse
 * @index_tags 考研英语, 首页查词, 单词检索, 多例句
 * @author holic512
 */
package org.example.backend.biz.kyyy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.backend.biz.kyyy.dto.KyyyHomeWordDetailResponse;
import org.example.backend.biz.kyyy.dto.KyyyHomeWordSearchResponse;
import org.example.backend.biz.kyyy.dto.KyyyWordExampleResponse;
import org.example.backend.biz.kyyy.entity.KyyyUserWordFavorite;
import org.example.backend.biz.kyyy.entity.KyyyWord;
import org.example.backend.biz.kyyy.entity.KyyyWordExample;
import org.example.backend.biz.kyyy.mapper.KyyyUserWordFavoriteMapper;
import org.example.backend.biz.kyyy.mapper.KyyyWordExampleMapper;
import org.example.backend.biz.kyyy.mapper.KyyyWordMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class KyyyHomeWordSearchService {

    private static final int RESULT_LIMIT = 12;
    private static final int EXAMPLE_LIMIT = 5;

    private final KyyyWordMapper kyyyWordMapper;
    private final KyyyWordExampleMapper kyyyWordExampleMapper;
    private final KyyyUserWordFavoriteMapper kyyyUserWordFavoriteMapper;

    public KyyyHomeWordSearchService(KyyyWordMapper kyyyWordMapper,
                                     KyyyWordExampleMapper kyyyWordExampleMapper,
                                     KyyyUserWordFavoriteMapper kyyyUserWordFavoriteMapper) {
        this.kyyyWordMapper = kyyyWordMapper;
        this.kyyyWordExampleMapper = kyyyWordExampleMapper;
        this.kyyyUserWordFavoriteMapper = kyyyUserWordFavoriteMapper;
    }

    public List<KyyyHomeWordSearchResponse> searchWords(Long userId, String keyword) {
        String displayKeyword = normalizeDisplayKeyword(keyword);
        String normalizedKeyword = normalizeSearchKeyword(displayKeyword);
        if (!StringUtils.hasText(normalizedKeyword)) {
            return List.of();
        }

        Map<Long, KyyyHomeWordSearchResponse> results = new LinkedHashMap<>();
        appendResults(results, queryExactWords(displayKeyword, normalizedKeyword));
        if (results.size() < RESULT_LIMIT) {
            appendResults(results, queryPrefixWords(displayKeyword, normalizedKeyword));
        }
        if (results.size() < RESULT_LIMIT) {
            appendResults(results, queryContainsWords(displayKeyword, normalizedKeyword));
        }
        Set<Long> favoriteWordIds = loadFavoriteWordIds(userId, results.keySet());
        return results.values().stream()
                .map(item -> new KyyyHomeWordSearchResponse(
                        item.getWordId(),
                        item.getWordText(),
                        item.getPhoneticUs(),
                        item.getPhoneticUk(),
                        item.getPartOfSpeech(),
                        item.getMeaningCn(),
                        favoriteWordIds.contains(item.getWordId())
                ))
                .limit(RESULT_LIMIT)
                .toList();
    }

    public KyyyHomeWordDetailResponse getWordDetail(Long userId, Long wordId) {
        if (wordId == null || wordId <= 0) {
            return null;
        }
        KyyyWord word = kyyyWordMapper.selectOne(detailQueryWrapper(wordId));
        if (word == null) {
            return null;
        }
        boolean isFavorite = loadFavoriteWordIds(userId, List.of(word.getId())).contains(word.getId());
        return toDetailResponse(word, isFavorite);
    }

    private void appendResults(Map<Long, KyyyHomeWordSearchResponse> results, List<KyyyWord> words) {
        if (results.size() >= RESULT_LIMIT || words == null || words.isEmpty()) {
            return;
        }
        for (KyyyWord word : words) {
            if (word == null || word.getId() == null || results.containsKey(word.getId())) {
                continue;
            }
            results.put(word.getId(), toResponse(word));
            if (results.size() >= RESULT_LIMIT) {
                return;
            }
        }
    }

    private List<KyyyWord> queryExactWords(String displayKeyword, String normalizedKeyword) {
        return kyyyWordMapper.selectList(baseQueryWrapper()
                .and(wrapper -> wrapper
                        .eq("normalized_word", normalizedKeyword)
                        .or()
                        .eq("word_text", displayKeyword))
                .orderByAsc("id")
                .last("limit " + RESULT_LIMIT));
    }

    private List<KyyyWord> queryPrefixWords(String displayKeyword, String normalizedKeyword) {
        return kyyyWordMapper.selectList(baseQueryWrapper()
                .and(wrapper -> wrapper
                        .likeRight("normalized_word", normalizedKeyword)
                        .or()
                        .likeRight("word_text", displayKeyword))
                .orderByAsc("id")
                .last("limit " + RESULT_LIMIT));
    }

    private List<KyyyWord> queryContainsWords(String displayKeyword, String normalizedKeyword) {
        return kyyyWordMapper.selectList(baseQueryWrapper()
                .and(wrapper -> wrapper
                        .like("normalized_word", normalizedKeyword)
                        .or()
                        .like("word_text", displayKeyword))
                .orderByAsc("id")
                .last("limit " + RESULT_LIMIT));
    }

    private QueryWrapper<KyyyWord> baseQueryWrapper() {
        return new QueryWrapper<KyyyWord>()
                .select(
                        "id",
                        "word_text",
                        "phonetic_us",
                        "phonetic_uk",
                        "part_of_speech",
                        "meaning_cn"
                )
                .eq("status", 1);
    }

    private QueryWrapper<KyyyWord> detailQueryWrapper(Long wordId) {
        return new QueryWrapper<KyyyWord>()
                .select(
                        "id",
                        "word_text",
                        "phonetic_us",
                        "phonetic_uk",
                        "part_of_speech",
                        "meaning_cn"
                )
                .eq("id", wordId)
                .eq("status", 1)
                .last("limit 1");
    }

    private KyyyHomeWordSearchResponse toResponse(KyyyWord word) {
        return new KyyyHomeWordSearchResponse(
                word.getId(),
                normalizeText(word.getWordText()),
                normalizeText(word.getPhoneticUs()),
                normalizeText(word.getPhoneticUk()),
                normalizeText(word.getPartOfSpeech()),
                normalizeText(word.getMeaningCn()),
                false
        );
    }

    private KyyyHomeWordDetailResponse toDetailResponse(KyyyWord word, boolean isFavorite) {
        List<KyyyWordExampleResponse> examples = buildExampleResponses(word);
        KyyyWordExampleResponse primaryExample = examples.isEmpty() ? null : examples.get(0);
        return new KyyyHomeWordDetailResponse(
                word.getId(),
                normalizeText(word.getWordText()),
                normalizeText(word.getPhoneticUs()),
                normalizeText(word.getPhoneticUk()),
                normalizeText(word.getPartOfSpeech()),
                normalizeText(word.getMeaningCn()),
                primaryExample == null ? "" : normalizeText(primaryExample.getExampleSentence()),
                primaryExample == null ? "" : normalizeText(primaryExample.getExampleTranslation()),
                examples,
                isFavorite
        );
    }

    private Set<Long> loadFavoriteWordIds(Long userId, Iterable<Long> wordIds) {
        if (userId == null || wordIds == null) {
            return Set.of();
        }
        Set<Long> normalizedWordIds = new HashSet<>();
        for (Long wordId : wordIds) {
            if (wordId != null && wordId > 0) {
                normalizedWordIds.add(wordId);
            }
        }
        if (normalizedWordIds.isEmpty()) {
            return Set.of();
        }
        return kyyyUserWordFavoriteMapper.selectList(new LambdaQueryWrapper<KyyyUserWordFavorite>()
                        .eq(KyyyUserWordFavorite::getUserId, userId)
                        .in(KyyyUserWordFavorite::getWordId, normalizedWordIds))
                .stream()
                .map(KyyyUserWordFavorite::getWordId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private List<KyyyWordExampleResponse> buildExampleResponses(KyyyWord word) {
        if (word == null || word.getId() == null) {
            return List.of();
        }
        List<KyyyWordExample> examples = kyyyWordExampleMapper.selectList(new QueryWrapper<KyyyWordExample>()
                .select("id", "example_sentence", "example_translation")
                .eq("word_id", word.getId())
                .eq("status", 1)
                .orderByAsc("sort_no")
                .orderByAsc("id")
                .last("limit " + EXAMPLE_LIMIT));
        List<KyyyWordExampleResponse> result = examples.stream()
                .map(this::toExampleResponse)
                .filter(item -> StringUtils.hasText(item.getExampleSentence()))
                .toList();
        return result;
    }

    private KyyyWordExampleResponse toExampleResponse(KyyyWordExample example) {
        return new KyyyWordExampleResponse(
                example.getId(),
                normalizeText(example.getExampleSentence()),
                normalizeText(example.getExampleTranslation())
        );
    }

    private String normalizeDisplayKeyword(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeSearchKeyword(String value) {
        return normalizeDisplayKeyword(value).toLowerCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }
}
