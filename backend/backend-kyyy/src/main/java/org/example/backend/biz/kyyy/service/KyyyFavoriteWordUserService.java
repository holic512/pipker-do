/**
 * @file KyyyFavoriteWordUserService
 * @project pipker-do
 * @module 考研英语 / 单词收藏
 * @description 管理首页查词场景的单词收藏列表、收藏与取消收藏逻辑。
 * @logic 1. 按用户读取收藏记录并关联有效单词；2. 过滤关键词并按收藏时间倒序；3. 收藏开关操作保持幂等。
 * @dependencies Mapper: KyyyUserWordFavoriteMapper, Mapper: KyyyWordMapper, DTO: KyyyFavoriteWordResponse
 * @index_tags 考研英语, 单词收藏服务, 收藏列表, 收藏开关
 * @author holic512
 */
package org.example.backend.biz.kyyy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.backend.biz.kyyy.dto.KyyyFavoriteWordRecordResponse;
import org.example.backend.biz.kyyy.dto.KyyyFavoriteWordResponse;
import org.example.backend.biz.kyyy.dto.KyyyFavoriteWordToggleResponse;
import org.example.backend.biz.kyyy.entity.KyyyUserWordFavorite;
import org.example.backend.biz.kyyy.entity.KyyyWord;
import org.example.backend.biz.kyyy.mapper.KyyyUserWordFavoriteMapper;
import org.example.backend.biz.kyyy.mapper.KyyyWordMapper;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class KyyyFavoriteWordUserService {

    private static final int ACTIVE_STATUS = 1;

    private final KyyyUserWordFavoriteMapper kyyyUserWordFavoriteMapper;
    private final KyyyWordMapper kyyyWordMapper;

    public KyyyFavoriteWordUserService(KyyyUserWordFavoriteMapper kyyyUserWordFavoriteMapper,
                                       KyyyWordMapper kyyyWordMapper) {
        this.kyyyUserWordFavoriteMapper = kyyyUserWordFavoriteMapper;
        this.kyyyWordMapper = kyyyWordMapper;
    }

    public KyyyFavoriteWordResponse getFavoriteWords(Long userId, String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        List<KyyyUserWordFavorite> favorites = kyyyUserWordFavoriteMapper.selectList(new LambdaQueryWrapper<KyyyUserWordFavorite>()
                .eq(KyyyUserWordFavorite::getUserId, userId)
                .orderByDesc(KyyyUserWordFavorite::getCreatedAt)
                .orderByDesc(KyyyUserWordFavorite::getId));
        if (favorites.isEmpty()) {
            return new KyyyFavoriteWordResponse(0, List.of());
        }
        Map<Long, KyyyWord> wordMap = loadActiveWordMap(favorites);
        List<KyyyFavoriteWordRecordResponse> records = favorites.stream()
                .map(item -> toRecord(item, wordMap))
                .filter(Objects::nonNull)
                .filter(item -> matchesKeyword(item, normalizedKeyword))
                .sorted(Comparator
                        .comparing(KyyyFavoriteWordRecordResponse::getFavoriteAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(KyyyFavoriteWordRecordResponse::getWordId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        return new KyyyFavoriteWordResponse(records.size(), records);
    }

    @Transactional
    public KyyyFavoriteWordToggleResponse favoriteWord(Long userId, Long wordId) {
        KyyyWord word = requireActiveWord(wordId);
        KyyyUserWordFavorite existing = selectFavorite(userId, word.getId());
        if (existing == null) {
            KyyyUserWordFavorite favorite = new KyyyUserWordFavorite();
            favorite.setUserId(userId);
            favorite.setWordId(word.getId());
            favorite.setCreatedAt(LocalDateTime.now());
            kyyyUserWordFavoriteMapper.insert(favorite);
        }
        return new KyyyFavoriteWordToggleResponse(word.getId(), true);
    }

    @Transactional
    public KyyyFavoriteWordToggleResponse unfavoriteWord(Long userId, Long wordId) {
        KyyyWord word = requireActiveWord(wordId);
        KyyyUserWordFavorite existing = selectFavorite(userId, word.getId());
        if (existing != null) {
            kyyyUserWordFavoriteMapper.deleteById(existing.getId());
        }
        return new KyyyFavoriteWordToggleResponse(word.getId(), false);
    }

    private Map<Long, KyyyWord> loadActiveWordMap(List<KyyyUserWordFavorite> favorites) {
        List<Long> wordIds = favorites.stream()
                .map(KyyyUserWordFavorite::getWordId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (wordIds.isEmpty()) {
            return Map.of();
        }
        return kyyyWordMapper.selectList(new LambdaQueryWrapper<KyyyWord>()
                        .in(KyyyWord::getId, wordIds)
                        .eq(KyyyWord::getStatus, ACTIVE_STATUS))
                .stream()
                .collect(Collectors.toMap(KyyyWord::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private KyyyFavoriteWordRecordResponse toRecord(KyyyUserWordFavorite favorite, Map<Long, KyyyWord> wordMap) {
        KyyyWord word = wordMap.get(favorite.getWordId());
        if (word == null) {
            return null;
        }
        return new KyyyFavoriteWordRecordResponse(
                word.getId(),
                normalizeText(word.getWordText()),
                normalizeText(word.getPhoneticUs()),
                normalizeText(word.getPhoneticUk()),
                normalizeText(word.getPartOfSpeech()),
                normalizeText(word.getMeaningCn()),
                favorite.getCreatedAt()
        );
    }

    private boolean matchesKeyword(KyyyFavoriteWordRecordResponse record, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String wordText = record.getWordText() == null ? "" : record.getWordText().toLowerCase(Locale.ROOT);
        String meaningCn = record.getMeaningCn() == null ? "" : record.getMeaningCn().toLowerCase(Locale.ROOT);
        String partOfSpeech = record.getPartOfSpeech() == null ? "" : record.getPartOfSpeech().toLowerCase(Locale.ROOT);
        return wordText.contains(keyword) || meaningCn.contains(keyword) || partOfSpeech.contains(keyword);
    }

    private KyyyUserWordFavorite selectFavorite(Long userId, Long wordId) {
        return kyyyUserWordFavoriteMapper.selectOne(new LambdaQueryWrapper<KyyyUserWordFavorite>()
                .eq(KyyyUserWordFavorite::getUserId, userId)
                .eq(KyyyUserWordFavorite::getWordId, wordId)
                .last("limit 1"));
    }

    private KyyyWord requireActiveWord(Long wordId) {
        if (wordId == null || wordId <= 0) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "单词不存在");
        }
        KyyyWord word = kyyyWordMapper.selectById(wordId);
        if (word == null || !Objects.equals(word.getStatus(), ACTIVE_STATUS)) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "单词不存在或已停用");
        }
        return word;
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return "";
        }
        return keyword.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }
}
