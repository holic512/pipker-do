package org.example.backend.biz.kyzz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.backend.biz.kyzz.dto.KyzzFavoriteQuestionRecordResponse;
import org.example.backend.biz.kyzz.dto.KyzzFavoriteQuestionResponse;
import org.example.backend.biz.kyzz.dto.KyzzFavoriteQuestionToggleResponse;
import org.example.backend.biz.kyzz.entity.KyzzQuestion;
import org.example.backend.biz.kyzz.entity.KyzzQuestionBank;
import org.example.backend.biz.kyzz.entity.KyzzUserFavorite;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionBankMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionMapper;
import org.example.backend.biz.kyzz.mapper.KyzzUserFavoriteMapper;
import org.example.backend.biz.kyzz.support.KyzzCacheService;
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

/**
 * AI 索引: KYZZ 用户侧题目收藏服务。
 */
@Service
public class KyzzFavoriteQuestionUserService {

    private final KyzzUserFavoriteMapper kyzzUserFavoriteMapper;
    private final KyzzQuestionMapper kyzzQuestionMapper;
    private final KyzzQuestionBankMapper kyzzQuestionBankMapper;
    private final KyzzCacheService kyzzCacheService;

    public KyzzFavoriteQuestionUserService(KyzzUserFavoriteMapper kyzzUserFavoriteMapper,
                                           KyzzQuestionMapper kyzzQuestionMapper,
                                           KyzzQuestionBankMapper kyzzQuestionBankMapper,
                                           KyzzCacheService kyzzCacheService) {
        this.kyzzUserFavoriteMapper = kyzzUserFavoriteMapper;
        this.kyzzQuestionMapper = kyzzQuestionMapper;
        this.kyzzQuestionBankMapper = kyzzQuestionBankMapper;
        this.kyzzCacheService = kyzzCacheService;
    }

    public KyzzFavoriteQuestionResponse getFavoriteQuestions(Long userId, String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        return kyzzCacheService.getOrLoad(
                kyzzCacheService.userFavoriteQuestionsKey(userId, normalizedKeyword),
                KyzzCacheService.USER_AGGREGATE_TTL,
                KyzzFavoriteQuestionResponse.class,
                () -> loadFavoriteQuestions(userId, normalizedKeyword)
        );
    }

    public boolean isFavorite(Long userId, Long questionId) {
        if (userId == null || questionId == null) {
            return false;
        }
        return kyzzUserFavoriteMapper.selectCount(new LambdaQueryWrapper<KyzzUserFavorite>()
                .eq(KyzzUserFavorite::getUserId, userId)
                .eq(KyzzUserFavorite::getTargetId, questionId)) > 0;
    }

    @Transactional
    public KyzzFavoriteQuestionToggleResponse favoriteQuestion(Long userId, Long questionId) {
        KyzzQuestion question = requireActiveQuestion(questionId);
        KyzzUserFavorite existing = selectFavorite(userId, question.getId());
        if (existing == null) {
            KyzzUserFavorite favorite = new KyzzUserFavorite();
            favorite.setUserId(userId);
            favorite.setTargetId(question.getId());
            favorite.setCreatedAt(LocalDateTime.now());
            kyzzUserFavoriteMapper.insert(favorite);
            kyzzCacheService.evictUserFavoriteCaches(userId);
        }
        return new KyzzFavoriteQuestionToggleResponse(question.getId(), true);
    }

    @Transactional
    public KyzzFavoriteQuestionToggleResponse unfavoriteQuestion(Long userId, Long questionId) {
        KyzzQuestion question = requireActiveQuestion(questionId);
        KyzzUserFavorite existing = selectFavorite(userId, question.getId());
        if (existing != null) {
            kyzzUserFavoriteMapper.deleteById(existing.getId());
            kyzzCacheService.evictUserFavoriteCaches(userId);
        }
        return new KyzzFavoriteQuestionToggleResponse(question.getId(), false);
    }

    private KyzzFavoriteQuestionResponse loadFavoriteQuestions(Long userId, String normalizedKeyword) {
        List<KyzzUserFavorite> favorites = kyzzUserFavoriteMapper.selectList(new LambdaQueryWrapper<KyzzUserFavorite>()
                .eq(KyzzUserFavorite::getUserId, userId)
                .orderByDesc(KyzzUserFavorite::getCreatedAt)
                .orderByDesc(KyzzUserFavorite::getId));
        if (favorites.isEmpty()) {
            return new KyzzFavoriteQuestionResponse(0, List.of());
        }

        Map<Long, KyzzQuestion> questionMap = loadActiveQuestionMap(favorites);
        Map<Long, KyzzQuestionBank> bankMap = loadActiveBankMap(questionMap);
        List<KyzzFavoriteQuestionRecordResponse> records = favorites.stream()
                .map(item -> toRecord(item, questionMap, bankMap))
                .filter(Objects::nonNull)
                .filter(item -> matchesKeyword(item, normalizedKeyword))
                .sorted(Comparator
                        .comparing(KyzzFavoriteQuestionRecordResponse::getFavoriteAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(KyzzFavoriteQuestionRecordResponse::getQuestionId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        return new KyzzFavoriteQuestionResponse(records.size(), records);
    }

    private Map<Long, KyzzQuestion> loadActiveQuestionMap(List<KyzzUserFavorite> favorites) {
        List<Long> questionIds = favorites.stream()
                .map(KyzzUserFavorite::getTargetId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (questionIds.isEmpty()) {
            return Map.of();
        }
        return kyzzQuestionMapper.selectList(new LambdaQueryWrapper<KyzzQuestion>()
                        .in(KyzzQuestion::getId, questionIds)
                        .eq(KyzzQuestion::getStatus, 1))
                .stream()
                .collect(Collectors.toMap(KyzzQuestion::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, KyzzQuestionBank> loadActiveBankMap(Map<Long, KyzzQuestion> questionMap) {
        List<Long> bankIds = questionMap.values().stream()
                .map(KyzzQuestion::getQuestionBankId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (bankIds.isEmpty()) {
            return Map.of();
        }
        return kyzzQuestionBankMapper.selectList(new LambdaQueryWrapper<KyzzQuestionBank>()
                        .in(KyzzQuestionBank::getId, bankIds)
                        .eq(KyzzQuestionBank::getStatus, 1))
                .stream()
                .collect(Collectors.toMap(KyzzQuestionBank::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private KyzzFavoriteQuestionRecordResponse toRecord(KyzzUserFavorite favorite,
                                                        Map<Long, KyzzQuestion> questionMap,
                                                        Map<Long, KyzzQuestionBank> bankMap) {
        KyzzQuestion question = questionMap.get(favorite.getTargetId());
        if (question == null) {
            return null;
        }
        KyzzQuestionBank bank = bankMap.get(question.getQuestionBankId());
        if (bank == null) {
            return null;
        }
        return new KyzzFavoriteQuestionRecordResponse(
                question.getId(),
                bank.getId(),
                bank.getBankName(),
                question.getQuestionType(),
                question.getStem(),
                question.getDifficultyLevel(),
                favorite.getCreatedAt()
        );
    }

    private boolean matchesKeyword(KyzzFavoriteQuestionRecordResponse record, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String stem = record.getStem() == null ? "" : record.getStem().toLowerCase(Locale.ROOT);
        String bankName = record.getBankName() == null ? "" : record.getBankName().toLowerCase(Locale.ROOT);
        return stem.contains(keyword) || bankName.contains(keyword);
    }

    private KyzzUserFavorite selectFavorite(Long userId, Long questionId) {
        return kyzzUserFavoriteMapper.selectOne(new LambdaQueryWrapper<KyzzUserFavorite>()
                .eq(KyzzUserFavorite::getUserId, userId)
                .eq(KyzzUserFavorite::getTargetId, questionId)
                .last("LIMIT 1"));
    }

    private KyzzQuestion requireActiveQuestion(Long questionId) {
        if (questionId == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题目不存在");
        }
        KyzzQuestion question = kyzzQuestionMapper.selectById(questionId);
        if (question == null || !Objects.equals(question.getStatus(), 1)) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "题目不存在或已下架");
        }
        return question;
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return "";
        }
        return keyword.trim().toLowerCase(Locale.ROOT);
    }
}
