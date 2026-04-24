package org.example.backend.biz.kyzz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.backend.biz.kyzz.dto.KyzzWrongQuestionRecordResponse;
import org.example.backend.biz.kyzz.dto.KyzzWrongQuestionResponse;
import org.example.backend.biz.kyzz.dto.KyzzWrongQuestionSummaryResponse;
import org.example.backend.biz.kyzz.entity.KyzzQuestion;
import org.example.backend.biz.kyzz.entity.KyzzQuestionBank;
import org.example.backend.biz.kyzz.entity.KyzzUserWrongQuestion;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionBankMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionMapper;
import org.example.backend.biz.kyzz.mapper.KyzzUserWrongQuestionMapper;
import org.example.backend.biz.kyzz.support.KyzzCacheService;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI 索引: KYZZ 用户侧错题本服务。
 */
@Service
public class KyzzWrongQuestionUserService {

    private static final String STATUS_ALL = "all";
    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_MASTERED = "mastered";
    private static final Set<String> SUPPORTED_STATUS = Set.of(STATUS_ALL, STATUS_ACTIVE, STATUS_MASTERED);

    private final KyzzUserWrongQuestionMapper kyzzUserWrongQuestionMapper;
    private final KyzzQuestionMapper kyzzQuestionMapper;
    private final KyzzQuestionBankMapper kyzzQuestionBankMapper;
    private final KyzzCacheService kyzzCacheService;

    public KyzzWrongQuestionUserService(KyzzUserWrongQuestionMapper kyzzUserWrongQuestionMapper,
                                        KyzzQuestionMapper kyzzQuestionMapper,
                                        KyzzQuestionBankMapper kyzzQuestionBankMapper,
                                        KyzzCacheService kyzzCacheService) {
        this.kyzzUserWrongQuestionMapper = kyzzUserWrongQuestionMapper;
        this.kyzzQuestionMapper = kyzzQuestionMapper;
        this.kyzzQuestionBankMapper = kyzzQuestionBankMapper;
        this.kyzzCacheService = kyzzCacheService;
    }

    public KyzzWrongQuestionResponse getWrongQuestions(Long userId, String status, String keyword) {
        String normalizedStatus = normalizeStatus(status);
        String normalizedKeyword = normalizeKeyword(keyword);
        return kyzzCacheService.getOrLoad(
                kyzzCacheService.userWrongQuestionsKey(userId, normalizedStatus, normalizedKeyword),
                KyzzCacheService.USER_AGGREGATE_TTL,
                KyzzWrongQuestionResponse.class,
                () -> loadWrongQuestions(userId, normalizedStatus, normalizedKeyword)
        );
    }

    private KyzzWrongQuestionResponse loadWrongQuestions(Long userId, String normalizedStatus, String normalizedKeyword) {
        List<KyzzUserWrongQuestion> wrongQuestions = kyzzUserWrongQuestionMapper.selectList(new LambdaQueryWrapper<KyzzUserWrongQuestion>()
                .eq(KyzzUserWrongQuestion::getUserId, userId)
                .orderByDesc(KyzzUserWrongQuestion::getLastWrongAt)
                .orderByDesc(KyzzUserWrongQuestion::getId));
        if (wrongQuestions.isEmpty()) {
            return new KyzzWrongQuestionResponse(new KyzzWrongQuestionSummaryResponse(0, 0, 0, 0), List.of());
        }

        Map<Long, KyzzQuestion> questionMap = loadActiveQuestionMap(wrongQuestions);
        Map<Long, KyzzQuestionBank> bankMap = loadActiveBankMap(wrongQuestions, questionMap);

        List<KyzzWrongQuestionRecordResponse> resolvedRecords = wrongQuestions.stream()
                .map(item -> toRecord(item, questionMap, bankMap))
                .filter(Objects::nonNull)
                .sorted(this::compareWrongQuestionRecord)
                .toList();

        KyzzWrongQuestionSummaryResponse summary = buildSummary(resolvedRecords);
        List<KyzzWrongQuestionRecordResponse> filteredRecords = resolvedRecords.stream()
                .filter(item -> matchesStatus(item, normalizedStatus))
                .filter(item -> matchesKeyword(item, normalizedKeyword))
                .toList();
        return new KyzzWrongQuestionResponse(summary, filteredRecords);
    }

    private Map<Long, KyzzQuestion> loadActiveQuestionMap(List<KyzzUserWrongQuestion> wrongQuestions) {
        List<Long> questionIds = wrongQuestions.stream()
                .map(KyzzUserWrongQuestion::getQuestionId)
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
                .collect(Collectors.toMap(
                        KyzzQuestion::getId,
                        item -> item,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private Map<Long, KyzzQuestionBank> loadActiveBankMap(List<KyzzUserWrongQuestion> wrongQuestions,
                                                          Map<Long, KyzzQuestion> questionMap) {
        List<Long> bankIds = wrongQuestions.stream()
                .map(item -> {
                    KyzzQuestion question = questionMap.get(item.getQuestionId());
                    if (question != null && question.getQuestionBankId() != null) {
                        return question.getQuestionBankId();
                    }
                    return item.getQuestionBankId();
                })
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
                .collect(Collectors.toMap(
                        KyzzQuestionBank::getId,
                        item -> item,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private KyzzWrongQuestionSummaryResponse buildSummary(List<KyzzWrongQuestionRecordResponse> records) {
        int totalCount = records.size();
        int masteredCount = (int) records.stream()
                .filter(item -> Boolean.TRUE.equals(item.getIsMastered()))
                .count();
        int totalWrongTimes = records.stream()
                .map(KyzzWrongQuestionRecordResponse::getWrongCount)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);
        return new KyzzWrongQuestionSummaryResponse(
                totalCount,
                totalCount - masteredCount,
                masteredCount,
                totalWrongTimes
        );
    }

    private KyzzWrongQuestionRecordResponse toRecord(KyzzUserWrongQuestion wrongQuestion,
                                                     Map<Long, KyzzQuestion> questionMap,
                                                     Map<Long, KyzzQuestionBank> bankMap) {
        KyzzQuestion question = questionMap.get(wrongQuestion.getQuestionId());
        if (question == null) {
            return null;
        }
        Long resolvedBankId = question.getQuestionBankId() != null ? question.getQuestionBankId() : wrongQuestion.getQuestionBankId();
        KyzzQuestionBank bank = bankMap.get(resolvedBankId);
        if (bank == null) {
            return null;
        }
        return new KyzzWrongQuestionRecordResponse(
                question.getId(),
                bank.getId(),
                bank.getBankName(),
                question.getQuestionType(),
                question.getStem(),
                question.getDifficultyLevel(),
                wrongQuestion.getWrongCount() == null ? 0 : wrongQuestion.getWrongCount(),
                wrongQuestion.getLastWrongAt(),
                Objects.equals(wrongQuestion.getIsMastered(), 1),
                wrongQuestion.getMasteredAt()
        );
    }

    private boolean matchesStatus(KyzzWrongQuestionRecordResponse record, String status) {
        if (STATUS_ALL.equals(status)) {
            return true;
        }
        boolean mastered = Boolean.TRUE.equals(record.getIsMastered());
        if (STATUS_ACTIVE.equals(status)) {
            return !mastered;
        }
        return mastered;
    }

    private boolean matchesKeyword(KyzzWrongQuestionRecordResponse record, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String stem = record.getStem() == null ? "" : record.getStem().toLowerCase(Locale.ROOT);
        String bankName = record.getBankName() == null ? "" : record.getBankName().toLowerCase(Locale.ROOT);
        return stem.contains(keyword) || bankName.contains(keyword);
    }

    private int compareWrongQuestionRecord(KyzzWrongQuestionRecordResponse left,
                                           KyzzWrongQuestionRecordResponse right) {
        int statusCompare = Boolean.compare(Boolean.TRUE.equals(left.getIsMastered()), Boolean.TRUE.equals(right.getIsMastered()));
        if (statusCompare != 0) {
            return statusCompare;
        }
        int lastWrongCompare = Comparator.<LocalDateTime>nullsLast(Comparator.reverseOrder()).compare(left.getLastWrongAt(), right.getLastWrongAt());
        if (lastWrongCompare != 0) {
            return lastWrongCompare;
        }
        int wrongCountCompare = Comparator.<Integer>nullsLast(Comparator.reverseOrder()).compare(left.getWrongCount(), right.getWrongCount());
        if (wrongCountCompare != 0) {
            return wrongCountCompare;
        }
        return Comparator.<Long>nullsLast(Comparator.reverseOrder()).compare(left.getQuestionId(), right.getQuestionId());
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return STATUS_ALL;
        }
        String normalized = status.trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_STATUS.contains(normalized)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "错题筛选状态不支持");
        }
        return normalized;
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return "";
        }
        return keyword.trim().toLowerCase(Locale.ROOT);
    }
}
