/**
 * @file KyyyWrongQuestionUserService
 * @project pipker-do
 * @module 考研英语 / 阅读错题本
 * @description 读取用户阅读错题聚合记录并组装错题本列表与汇总数据。
 * @logic 1. 按用户读取阅读错题记录；2. 关联有效题目与篇章信息；3. 完成状态筛选、关键词过滤和排序。
 * @dependencies Mapper: KyyyUserReadingWrongQuestionMapper, Mapper: KyyyReadingQuestionMapper, Mapper: KyyyReadingPassageMapper, Support: KyyyExamDirectionSupport
 * @index_tags 考研英语, 阅读错题本服务, 用户错题列表, 筛选排序
 * @author holic512
 */
package org.example.backend.biz.kyyy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.backend.biz.kyyy.dto.KyyyWrongQuestionRecordResponse;
import org.example.backend.biz.kyyy.dto.KyyyWrongQuestionResponse;
import org.example.backend.biz.kyyy.dto.KyyyWrongQuestionSummaryResponse;
import org.example.backend.biz.kyyy.entity.KyyyReadingPassage;
import org.example.backend.biz.kyyy.entity.KyyyReadingQuestion;
import org.example.backend.biz.kyyy.entity.KyyyUserReadingWrongQuestion;
import org.example.backend.biz.kyyy.mapper.KyyyReadingPassageMapper;
import org.example.backend.biz.kyyy.mapper.KyyyReadingQuestionMapper;
import org.example.backend.biz.kyyy.mapper.KyyyUserReadingWrongQuestionMapper;
import org.example.backend.biz.kyyy.support.KyyyExamDirectionSupport;
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

@Service
public class KyyyWrongQuestionUserService {

    private static final int ACTIVE_STATUS = 1;
    private static final String STATUS_ALL = "all";
    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_MASTERED = "mastered";
    private static final Set<String> SUPPORTED_STATUS = Set.of(STATUS_ALL, STATUS_ACTIVE, STATUS_MASTERED);

    private final KyyyUserReadingWrongQuestionMapper kyyyUserReadingWrongQuestionMapper;
    private final KyyyReadingQuestionMapper kyyyReadingQuestionMapper;
    private final KyyyReadingPassageMapper kyyyReadingPassageMapper;

    public KyyyWrongQuestionUserService(KyyyUserReadingWrongQuestionMapper kyyyUserReadingWrongQuestionMapper,
                                        KyyyReadingQuestionMapper kyyyReadingQuestionMapper,
                                        KyyyReadingPassageMapper kyyyReadingPassageMapper) {
        this.kyyyUserReadingWrongQuestionMapper = kyyyUserReadingWrongQuestionMapper;
        this.kyyyReadingQuestionMapper = kyyyReadingQuestionMapper;
        this.kyyyReadingPassageMapper = kyyyReadingPassageMapper;
    }

    public KyyyWrongQuestionResponse getWrongQuestions(Long userId, String status, String keyword) {
        String normalizedStatus = normalizeStatus(status);
        String normalizedKeyword = normalizeKeyword(keyword);
        return loadWrongQuestions(userId, normalizedStatus, normalizedKeyword);
    }

    private KyyyWrongQuestionResponse loadWrongQuestions(Long userId, String normalizedStatus, String normalizedKeyword) {
        List<KyyyUserReadingWrongQuestion> wrongQuestions = kyyyUserReadingWrongQuestionMapper.selectList(
                new LambdaQueryWrapper<KyyyUserReadingWrongQuestion>()
                        .eq(KyyyUserReadingWrongQuestion::getUserId, userId)
                        .orderByDesc(KyyyUserReadingWrongQuestion::getLastWrongAt)
                        .orderByDesc(KyyyUserReadingWrongQuestion::getId)
        );
        if (wrongQuestions.isEmpty()) {
            return new KyyyWrongQuestionResponse(
                    new KyyyWrongQuestionSummaryResponse(0, 0, 0, 0),
                    List.of()
            );
        }

        Map<Long, KyyyReadingQuestion> questionMap = loadActiveQuestionMap(wrongQuestions);
        Map<Long, KyyyReadingPassage> passageMap = loadActivePassageMap(wrongQuestions, questionMap);

        List<KyyyWrongQuestionRecordResponse> resolvedRecords = wrongQuestions.stream()
                .map(item -> toRecord(item, questionMap, passageMap))
                .filter(Objects::nonNull)
                .sorted(this::compareWrongQuestionRecord)
                .toList();

        KyyyWrongQuestionSummaryResponse summary = buildSummary(resolvedRecords);
        List<KyyyWrongQuestionRecordResponse> filteredRecords = resolvedRecords.stream()
                .filter(item -> matchesStatus(item, normalizedStatus))
                .filter(item -> matchesKeyword(item, normalizedKeyword))
                .toList();
        return new KyyyWrongQuestionResponse(summary, filteredRecords);
    }

    private Map<Long, KyyyReadingQuestion> loadActiveQuestionMap(List<KyyyUserReadingWrongQuestion> wrongQuestions) {
        List<Long> questionIds = wrongQuestions.stream()
                .map(KyyyUserReadingWrongQuestion::getQuestionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (questionIds.isEmpty()) {
            return Map.of();
        }
        return kyyyReadingQuestionMapper.selectList(new LambdaQueryWrapper<KyyyReadingQuestion>()
                        .in(KyyyReadingQuestion::getId, questionIds)
                        .eq(KyyyReadingQuestion::getStatus, ACTIVE_STATUS))
                .stream()
                .collect(Collectors.toMap(
                        KyyyReadingQuestion::getId,
                        item -> item,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private Map<Long, KyyyReadingPassage> loadActivePassageMap(List<KyyyUserReadingWrongQuestion> wrongQuestions,
                                                               Map<Long, KyyyReadingQuestion> questionMap) {
        List<Long> passageIds = wrongQuestions.stream()
                .map(item -> {
                    KyyyReadingQuestion question = questionMap.get(item.getQuestionId());
                    if (question != null && question.getPassageId() != null) {
                        return question.getPassageId();
                    }
                    return item.getPassageId();
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (passageIds.isEmpty()) {
            return Map.of();
        }
        return kyyyReadingPassageMapper.selectList(new LambdaQueryWrapper<KyyyReadingPassage>()
                        .in(KyyyReadingPassage::getId, passageIds)
                        .eq(KyyyReadingPassage::getStatus, ACTIVE_STATUS))
                .stream()
                .collect(Collectors.toMap(
                        KyyyReadingPassage::getId,
                        item -> item,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private KyyyWrongQuestionSummaryResponse buildSummary(List<KyyyWrongQuestionRecordResponse> records) {
        int totalCount = records.size();
        int masteredCount = (int) records.stream()
                .filter(item -> Boolean.TRUE.equals(item.getIsMastered()))
                .count();
        int totalWrongTimes = records.stream()
                .map(KyyyWrongQuestionRecordResponse::getWrongCount)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);
        return new KyyyWrongQuestionSummaryResponse(
                totalCount,
                totalCount - masteredCount,
                masteredCount,
                totalWrongTimes
        );
    }

    private KyyyWrongQuestionRecordResponse toRecord(KyyyUserReadingWrongQuestion wrongQuestion,
                                                     Map<Long, KyyyReadingQuestion> questionMap,
                                                     Map<Long, KyyyReadingPassage> passageMap) {
        KyyyReadingQuestion question = questionMap.get(wrongQuestion.getQuestionId());
        if (question == null) {
            return null;
        }
        Long resolvedPassageId = question.getPassageId() != null ? question.getPassageId() : wrongQuestion.getPassageId();
        KyyyReadingPassage passage = passageMap.get(resolvedPassageId);
        if (passage == null) {
            return null;
        }
        return new KyyyWrongQuestionRecordResponse(
                question.getId(),
                passage.getId(),
                passage.getSourceYear(),
                passage.getSourceName(),
                passage.getPassageNo(),
                passage.getExamDirection(),
                KyyyExamDirectionSupport.labelOf(passage.getExamDirection()),
                question.getStem(),
                wrongQuestion.getWrongCount() == null ? 0 : wrongQuestion.getWrongCount(),
                wrongQuestion.getLastWrongAt(),
                Objects.equals(wrongQuestion.getIsMastered(), 1),
                wrongQuestion.getMasteredAt()
        );
    }

    private boolean matchesStatus(KyyyWrongQuestionRecordResponse record, String status) {
        if (STATUS_ALL.equals(status)) {
            return true;
        }
        boolean mastered = Boolean.TRUE.equals(record.getIsMastered());
        if (STATUS_ACTIVE.equals(status)) {
            return !mastered;
        }
        return mastered;
    }

    private boolean matchesKeyword(KyyyWrongQuestionRecordResponse record, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String stem = record.getStem() == null ? "" : record.getStem().toLowerCase(Locale.ROOT);
        String sourceName = record.getSourceName() == null ? "" : record.getSourceName().toLowerCase(Locale.ROOT);
        return stem.contains(keyword) || sourceName.contains(keyword);
    }

    private int compareWrongQuestionRecord(KyyyWrongQuestionRecordResponse left,
                                           KyyyWrongQuestionRecordResponse right) {
        int statusCompare = Boolean.compare(Boolean.TRUE.equals(left.getIsMastered()), Boolean.TRUE.equals(right.getIsMastered()));
        if (statusCompare != 0) {
            return statusCompare;
        }
        int lastWrongCompare = Comparator.<LocalDateTime>nullsLast(Comparator.reverseOrder())
                .compare(left.getLastWrongAt(), right.getLastWrongAt());
        if (lastWrongCompare != 0) {
            return lastWrongCompare;
        }
        int wrongCountCompare = Comparator.<Integer>nullsLast(Comparator.reverseOrder())
                .compare(left.getWrongCount(), right.getWrongCount());
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
