package org.example.backend.biz.kyyy.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.backend.biz.kyyy.dto.KyyyHomeWordSearchResponse;
import org.example.backend.biz.kyyy.entity.KyyyWord;
import org.example.backend.biz.kyyy.mapper.KyyyWordMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @file KyyyHomeWordSearchService
 * @project pipker-do
 * @module 考研英语 / 首页查词
 * @description 为小程序首页提供轻量级单词检索能力。
 * @logic 1. 标准化查询词；2. 按精确、前缀、包含顺序查询有效单词；3. 去重并裁剪为首页列表结果。
 * @dependencies Mapper: KyyyWordMapper, DTO: KyyyHomeWordSearchResponse
 * @index_tags 考研英语, 首页查词, 单词检索, 词库
 * @author holic512
 */
@Service
public class KyyyHomeWordSearchService {

    private static final int RESULT_LIMIT = 12;

    private final KyyyWordMapper kyyyWordMapper;

    public KyyyHomeWordSearchService(KyyyWordMapper kyyyWordMapper) {
        this.kyyyWordMapper = kyyyWordMapper;
    }

    public List<KyyyHomeWordSearchResponse> searchWords(String keyword) {
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
		return results.values().stream()
				.limit(RESULT_LIMIT)
				.toList();
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

    private KyyyHomeWordSearchResponse toResponse(KyyyWord word) {
        return new KyyyHomeWordSearchResponse(
                word.getId(),
                normalizeText(word.getWordText()),
                normalizeText(word.getPhoneticUs()),
                normalizeText(word.getPhoneticUk()),
                normalizeText(word.getPartOfSpeech()),
                normalizeText(word.getMeaningCn())
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
