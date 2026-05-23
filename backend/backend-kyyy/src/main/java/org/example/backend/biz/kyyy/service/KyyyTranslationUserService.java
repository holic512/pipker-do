/**
 * @file KyyyTranslationUserService
 * @project pipker-do
 * @module 考研英语 / 翻译知识库
 * @description 编排翻译知识库首页总览、分页查询与详情读取逻辑。
 * @logic 1. 基于翻译主表生成方向、模式与年份分面；2. 支持按方向、模式、年份和关键词分页检索；3. 组合主表与分段表输出详情。
 * @dependencies Mapper: KyyyTranslationPassageMapper, Mapper: KyyyTranslationSegmentMapper
 * @index_tags 考研英语, 翻译服务, 知识库, 分页查询, 真题
 * @author holic512
 */
package org.example.backend.biz.kyyy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.backend.biz.kyyy.dto.KyyyTranslationCardResponse;
import org.example.backend.biz.kyyy.dto.KyyyTranslationDetailResponse;
import org.example.backend.biz.kyyy.dto.KyyyTranslationFacetResponse;
import org.example.backend.biz.kyyy.dto.KyyyTranslationListResponse;
import org.example.backend.biz.kyyy.dto.KyyyTranslationOverviewResponse;
import org.example.backend.biz.kyyy.dto.KyyyTranslationSegmentResponse;
import org.example.backend.biz.kyyy.entity.KyyyTranslationPassage;
import org.example.backend.biz.kyyy.entity.KyyyTranslationSegment;
import org.example.backend.biz.kyyy.mapper.KyyyTranslationPassageMapper;
import org.example.backend.biz.kyyy.mapper.KyyyTranslationSegmentMapper;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class KyyyTranslationUserService {

    private static final int ACTIVE_STATUS = 1;
    private static final long DEFAULT_PAGE_NO = 1L;
    private static final long DEFAULT_PAGE_SIZE = 10L;
    private static final long MAX_PAGE_SIZE = 20L;
    private static final int FEATURED_RECORD_LIMIT = 6;
    private static final Set<String> SUPPORTED_EXAM_DIRECTIONS = Set.of("english_one", "english_two");
    private static final Set<String> SUPPORTED_TRANSLATION_MODES = Set.of("segmented", "passage");

    private static final Map<String, String> EXAM_DIRECTION_LABELS = Map.of(
            "english_one", "英一",
            "english_two", "英二"
    );
    private static final Map<String, String> TRANSLATION_MODE_LABELS = Map.of(
            "segmented", "划线句翻译",
            "passage", "全文翻译"
    );

    private final KyyyTranslationPassageMapper translationPassageMapper;
    private final KyyyTranslationSegmentMapper translationSegmentMapper;

    public KyyyTranslationUserService(KyyyTranslationPassageMapper translationPassageMapper,
                                      KyyyTranslationSegmentMapper translationSegmentMapper) {
        this.translationPassageMapper = translationPassageMapper;
        this.translationSegmentMapper = translationSegmentMapper;
    }

    public KyyyTranslationOverviewResponse getOverview() {
        List<KyyyTranslationPassage> passages = translationPassageMapper.selectList(new LambdaQueryWrapper<KyyyTranslationPassage>()
                .eq(KyyyTranslationPassage::getStatus, ACTIVE_STATUS)
                .orderByDesc(KyyyTranslationPassage::getSourceYear)
                .orderByDesc(KyyyTranslationPassage::getSortNo)
                .orderByDesc(KyyyTranslationPassage::getId));
        if (passages.isEmpty()) {
            return new KyyyTranslationOverviewResponse(List.of(), List.of(), List.of(), List.of());
        }

        Map<String, Long> examDirectionCounter = countBy(passages, KyyyTranslationPassage::getExamDirection);
        Map<String, Long> translationModeCounter = countBy(passages, KyyyTranslationPassage::getTranslationMode);
        List<KyyyTranslationFacetResponse> examDirections = buildFacetResponses(
                List.of("english_one", "english_two"),
                examDirectionCounter,
                EXAM_DIRECTION_LABELS
        );
        List<KyyyTranslationFacetResponse> translationModes = buildFacetResponses(
                List.of("segmented", "passage"),
                translationModeCounter,
                TRANSLATION_MODE_LABELS
        );
        List<Integer> recentYears = passages.stream()
                .map(KyyyTranslationPassage::getSourceYear)
                .filter(year -> year != null && year > 0)
                .distinct()
                .sorted((left, right) -> Integer.compare(right, left))
                .toList();
        List<KyyyTranslationCardResponse> featuredRecords = passages.stream()
                .limit(FEATURED_RECORD_LIMIT)
                .map(this::toCard)
                .toList();
        return new KyyyTranslationOverviewResponse(examDirections, translationModes, recentYears, featuredRecords);
    }

    public KyyyTranslationListResponse getPassages(String examDirection,
                                                   String translationMode,
                                                   Integer sourceYear,
                                                   String keyword,
                                                   Long pageNo,
                                                   Long pageSize) {
        String normalizedExamDirection = normalizeExamDirection(examDirection);
        String normalizedTranslationMode = normalizeTranslationMode(translationMode);
        long normalizedPageNo = pageNo == null || pageNo < DEFAULT_PAGE_NO ? DEFAULT_PAGE_NO : pageNo;
        long normalizedPageSize = pageSize == null || pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        String normalizedKeyword = normalizeKeyword(keyword);

        LambdaQueryWrapper<KyyyTranslationPassage> wrapper = new LambdaQueryWrapper<KyyyTranslationPassage>()
                .eq(KyyyTranslationPassage::getStatus, ACTIVE_STATUS)
                .orderByDesc(KyyyTranslationPassage::getSourceYear)
                .orderByDesc(KyyyTranslationPassage::getSortNo)
                .orderByDesc(KyyyTranslationPassage::getId);
        if (normalizedExamDirection != null) {
            wrapper.eq(KyyyTranslationPassage::getExamDirection, normalizedExamDirection);
        }
        if (normalizedTranslationMode != null) {
            wrapper.eq(KyyyTranslationPassage::getTranslationMode, normalizedTranslationMode);
        }
        if (sourceYear != null && sourceYear > 0) {
            wrapper.eq(KyyyTranslationPassage::getSourceYear, sourceYear);
        }
        if (StringUtils.hasText(normalizedKeyword)) {
            wrapper.and(query -> query
                    .like(KyyyTranslationPassage::getSourceTitle, normalizedKeyword)
                    .or()
                    .like(KyyyTranslationPassage::getPromptContent, normalizedKeyword)
                    .or()
                    .like(KyyyTranslationPassage::getKnowledgeTags, normalizedKeyword));
        }

        Page<KyyyTranslationPassage> page = translationPassageMapper.selectPage(new Page<>(normalizedPageNo, normalizedPageSize), wrapper);
        List<KyyyTranslationCardResponse> records = page.getRecords().stream()
                .map(this::toCard)
                .toList();
        return new KyyyTranslationListResponse(records, page.getCurrent(), page.getSize(), page.getCurrent() < page.getPages(), page.getTotal());
    }

    public KyyyTranslationDetailResponse getPassageDetail(Long passageId) {
        if (passageId == null || passageId <= 0) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "请选择要查看的翻译题");
        }
        KyyyTranslationPassage passage = translationPassageMapper.selectOne(new LambdaQueryWrapper<KyyyTranslationPassage>()
                .eq(KyyyTranslationPassage::getId, passageId)
                .eq(KyyyTranslationPassage::getStatus, ACTIVE_STATUS)
                .last("limit 1"));
        if (passage == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "翻译内容不存在或已停用");
        }
        List<KyyyTranslationSegmentResponse> segments = translationSegmentMapper.selectList(new LambdaQueryWrapper<KyyyTranslationSegment>()
                        .eq(KyyyTranslationSegment::getTranslationId, passage.getId())
                        .orderByAsc(KyyyTranslationSegment::getSortNo)
                        .orderByAsc(KyyyTranslationSegment::getId))
                .stream()
                .map(segment -> new KyyyTranslationSegmentResponse(
                        segment.getSegmentNo(),
                        segment.getSourceText(),
                        segment.getTranslatedText()
                ))
                .toList();
        return new KyyyTranslationDetailResponse(
                passage.getId(),
                passage.getTranslationCode(),
                passage.getSourceTitle(),
                passage.getExamDirection(),
                passage.getSourceYear(),
                passage.getTranslationMode(),
                passage.getScoreValue(),
                passage.getSegmentCount(),
                passage.getPromptInstruction(),
                passage.getPromptContent(),
                passage.getPromptTranslation(),
                passage.getReferenceTranslation(),
                passage.getReferenceNote(),
                segments,
                splitTags(passage.getKnowledgeTags()),
                passage.getSourcePath(),
                passage.getSourcePromptRef(),
                passage.getSourceAnswerRef()
        );
    }

    private Map<String, Long> countBy(List<KyyyTranslationPassage> passages,
                                      java.util.function.Function<KyyyTranslationPassage, String> extractor) {
        return passages.stream()
                .map(extractor)
                .filter(StringUtils::hasText)
                .collect(Collectors.groupingBy(value -> value, LinkedHashMap::new, Collectors.counting()));
    }

    private List<KyyyTranslationFacetResponse> buildFacetResponses(List<String> orderedCodes,
                                                                   Map<String, Long> counter,
                                                                   Map<String, String> labels) {
        List<KyyyTranslationFacetResponse> responses = new ArrayList<>();
        orderedCodes.forEach(code -> {
            long count = counter.getOrDefault(code, 0L);
            if (count > 0) {
                responses.add(new KyyyTranslationFacetResponse(code, labels.getOrDefault(code, code), count));
            }
        });
        return responses;
    }

    private KyyyTranslationCardResponse toCard(KyyyTranslationPassage passage) {
        return new KyyyTranslationCardResponse(
                passage.getId(),
                passage.getTranslationCode(),
                passage.getSourceTitle(),
                passage.getExamDirection(),
                passage.getSourceYear(),
                passage.getTranslationMode(),
                passage.getScoreValue(),
                passage.getSegmentCount(),
                splitTags(passage.getKnowledgeTags())
        );
    }

    private List<String> splitTags(String rawTags) {
        if (!StringUtils.hasText(rawTags)) {
            return List.of();
        }
        return Arrays.stream(rawTags.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    private String normalizeExamDirection(String examDirection) {
        if (!StringUtils.hasText(examDirection)) {
            return null;
        }
        String normalized = examDirection.trim().toLowerCase();
        if (!SUPPORTED_EXAM_DIRECTIONS.contains(normalized)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "不支持的考试方向");
        }
        return normalized;
    }

    private String normalizeTranslationMode(String translationMode) {
        if (!StringUtils.hasText(translationMode)) {
            return null;
        }
        String normalized = translationMode.trim().toLowerCase();
        if (!SUPPORTED_TRANSLATION_MODES.contains(normalized)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "不支持的翻译模式");
        }
        return normalized;
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        String normalized = keyword.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
