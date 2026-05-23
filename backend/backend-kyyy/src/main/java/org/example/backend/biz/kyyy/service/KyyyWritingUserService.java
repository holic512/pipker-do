/**
 * @file KyyyWritingUserService
 * @project pipker-do
 * @module 考研英语 / 作文知识库
 * @description 编排作文知识库首页总览、分页查询与详情读取逻辑。
 * @logic 1. 基于作文主表生成方向、分区、题型与年份分面；2. 支持按方向、分区、题型、年份和关键词分页检索；3. 将标签字符串拆分为前端可读数组。
 * @dependencies Mapper: KyyyWritingEssayMapper, Entity: KyyyWritingEssay
 * @index_tags 考研英语, 作文服务, 知识库, 分页查询, 真题
 * @author holic512
 */
package org.example.backend.biz.kyyy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.backend.biz.kyyy.dto.KyyyWritingEssayCardResponse;
import org.example.backend.biz.kyyy.dto.KyyyWritingEssayDetailResponse;
import org.example.backend.biz.kyyy.dto.KyyyWritingEssayListResponse;
import org.example.backend.biz.kyyy.dto.KyyyWritingFacetResponse;
import org.example.backend.biz.kyyy.dto.KyyyWritingOverviewResponse;
import org.example.backend.biz.kyyy.entity.KyyyWritingEssay;
import org.example.backend.biz.kyyy.mapper.KyyyWritingEssayMapper;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class KyyyWritingUserService {

    private static final int ACTIVE_STATUS = 1;
    private static final long DEFAULT_PAGE_NO = 1L;
    private static final long DEFAULT_PAGE_SIZE = 10L;
    private static final long MAX_PAGE_SIZE = 20L;
    private static final int FEATURED_RECORD_LIMIT = 6;
    private static final Set<String> SUPPORTED_EXAM_DIRECTIONS = Set.of("english_one", "english_two");
    private static final Set<String> SUPPORTED_ESSAY_SECTIONS = Set.of("small", "big");
    private static final Set<String> SUPPORTED_PROMPT_CATEGORIES = Set.of(
            "email",
            "letter",
            "notice",
            "memo",
            "report",
            "practical_writing",
            "chart",
            "picture",
            "essay"
    );

    private static final Map<String, String> EXAM_DIRECTION_LABELS = Map.of(
            "english_one", "英一",
            "english_two", "英二"
    );
    private static final Map<String, String> ESSAY_SECTION_LABELS = Map.of(
            "small", "小作文",
            "big", "大作文"
    );
    private static final Map<String, String> PROMPT_CATEGORY_LABELS = Map.ofEntries(
            Map.entry("email", "Email"),
            Map.entry("letter", "Letter"),
            Map.entry("notice", "Notice"),
            Map.entry("memo", "Memo"),
            Map.entry("report", "Report"),
            Map.entry("practical_writing", "应用文"),
            Map.entry("chart", "Chart"),
            Map.entry("picture", "Picture"),
            Map.entry("essay", "Essay")
    );

    private final KyyyWritingEssayMapper kyyyWritingEssayMapper;

    public KyyyWritingUserService(KyyyWritingEssayMapper kyyyWritingEssayMapper) {
        this.kyyyWritingEssayMapper = kyyyWritingEssayMapper;
    }

    public KyyyWritingOverviewResponse getOverview() {
        List<KyyyWritingEssay> essays = kyyyWritingEssayMapper.selectList(new LambdaQueryWrapper<KyyyWritingEssay>()
                .eq(KyyyWritingEssay::getStatus, ACTIVE_STATUS)
                .orderByDesc(KyyyWritingEssay::getSourceYear)
                .orderByAsc(KyyyWritingEssay::getEssaySection)
                .orderByDesc(KyyyWritingEssay::getSortNo)
                .orderByDesc(KyyyWritingEssay::getId));
        if (essays.isEmpty()) {
            return new KyyyWritingOverviewResponse(List.of(), List.of(), List.of(), List.of(), List.of());
        }

        Map<String, Long> examDirectionCounter = countBy(essays, KyyyWritingEssay::getExamDirection);
        Map<String, Long> essaySectionCounter = countBy(essays, KyyyWritingEssay::getEssaySection);
        Map<String, Long> promptCategoryCounter = countByCompositePromptCategory(essays);
        List<KyyyWritingFacetResponse> examDirections = buildFacetResponses(
                List.of("english_one", "english_two"),
                examDirectionCounter,
                EXAM_DIRECTION_LABELS,
                null
        );
        List<KyyyWritingFacetResponse> essaySections = buildFacetResponses(
                List.of("small", "big"),
                essaySectionCounter,
                ESSAY_SECTION_LABELS,
                null
        );
        List<KyyyWritingFacetResponse> promptCategories = buildPromptCategoryFacets(promptCategoryCounter);
        List<Integer> recentYears = essays.stream()
                .map(KyyyWritingEssay::getSourceYear)
                .filter(year -> year != null && year > 0)
                .distinct()
                .sorted((left, right) -> Integer.compare(right, left))
                .toList();
        List<KyyyWritingEssayCardResponse> featuredRecords = essays.stream()
                .limit(FEATURED_RECORD_LIMIT)
                .map(this::toEssayCard)
                .toList();
        return new KyyyWritingOverviewResponse(
                examDirections,
                essaySections,
                promptCategories,
                recentYears,
                featuredRecords
        );
    }

    public KyyyWritingEssayListResponse getEssays(String examDirection,
                                                  String essaySection,
                                                  String promptCategory,
                                                  Integer sourceYear,
                                                  String keyword,
                                                  Long pageNo,
                                                  Long pageSize) {
        String normalizedExamDirection = normalizeExamDirection(examDirection);
        String normalizedEssaySection = normalizeEssaySection(essaySection);
        String normalizedPromptCategory = normalizePromptCategory(promptCategory);
        long normalizedPageNo = pageNo == null || pageNo < DEFAULT_PAGE_NO ? DEFAULT_PAGE_NO : pageNo;
        long normalizedPageSize = pageSize == null || pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        String normalizedKeyword = normalizeKeyword(keyword);

        LambdaQueryWrapper<KyyyWritingEssay> wrapper = new LambdaQueryWrapper<KyyyWritingEssay>()
                .eq(KyyyWritingEssay::getStatus, ACTIVE_STATUS)
                .orderByDesc(KyyyWritingEssay::getSourceYear)
                .orderByAsc(KyyyWritingEssay::getEssaySection)
                .orderByDesc(KyyyWritingEssay::getSortNo)
                .orderByDesc(KyyyWritingEssay::getId);
        if (normalizedExamDirection != null) {
            wrapper.eq(KyyyWritingEssay::getExamDirection, normalizedExamDirection);
        }
        if (normalizedEssaySection != null) {
            wrapper.eq(KyyyWritingEssay::getEssaySection, normalizedEssaySection);
        }
        if (normalizedPromptCategory != null) {
            wrapper.eq(KyyyWritingEssay::getPromptCategory, normalizedPromptCategory);
        }
        if (sourceYear != null && sourceYear > 0) {
            wrapper.eq(KyyyWritingEssay::getSourceYear, sourceYear);
        }
        if (StringUtils.hasText(normalizedKeyword)) {
            wrapper.and(query -> query
                    .like(KyyyWritingEssay::getSourceTitle, normalizedKeyword)
                    .or()
                    .like(KyyyWritingEssay::getPromptContent, normalizedKeyword)
                    .or()
                    .like(KyyyWritingEssay::getKnowledgeTags, normalizedKeyword));
        }

        Page<KyyyWritingEssay> page = kyyyWritingEssayMapper.selectPage(new Page<>(normalizedPageNo, normalizedPageSize), wrapper);
        List<KyyyWritingEssayCardResponse> records = page.getRecords().stream()
                .map(this::toEssayCard)
                .toList();
        return new KyyyWritingEssayListResponse(
                records,
                page.getCurrent(),
                page.getSize(),
                page.getCurrent() < page.getPages(),
                page.getTotal()
        );
    }

    public KyyyWritingEssayDetailResponse getEssayDetail(Long essayId) {
        if (essayId == null || essayId <= 0) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "请选择要查看的作文");
        }
        KyyyWritingEssay essay = kyyyWritingEssayMapper.selectOne(new LambdaQueryWrapper<KyyyWritingEssay>()
                .eq(KyyyWritingEssay::getId, essayId)
                .eq(KyyyWritingEssay::getStatus, ACTIVE_STATUS)
                .last("limit 1"));
        if (essay == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "作文内容不存在或已停用");
        }
        return new KyyyWritingEssayDetailResponse(
                essay.getId(),
                essay.getWritingCode(),
                essay.getSourceTitle(),
                essay.getExamDirection(),
                essay.getSourceYear(),
                essay.getEssaySection(),
                essay.getPromptCategory(),
                essay.getScoreValue(),
                essay.getWordLimitMin(),
                essay.getWordLimitMax(),
                essay.getPromptContent(),
                essay.getPromptTranslation(),
                essay.getSampleContent(),
                essay.getSampleTranslation(),
                splitTags(essay.getKnowledgeTags()),
                essay.getSourcePath()
        );
    }

    private Map<String, Long> countBy(List<KyyyWritingEssay> essays, java.util.function.Function<KyyyWritingEssay, String> extractor) {
        return essays.stream()
                .map(extractor)
                .filter(StringUtils::hasText)
                .collect(Collectors.groupingBy(value -> value, LinkedHashMap::new, Collectors.counting()));
    }

    private Map<String, Long> countByCompositePromptCategory(List<KyyyWritingEssay> essays) {
        Map<String, Long> counter = new LinkedHashMap<>();
        essays.forEach(essay -> {
            String category = normalizePromptCategory(essay.getPromptCategory());
            String section = normalizeEssaySection(essay.getEssaySection());
            if (category == null || section == null) {
                return;
            }
            String compositeKey = section + ":" + category;
            counter.put(compositeKey, counter.getOrDefault(compositeKey, 0L) + 1L);
        });
        return counter;
    }

    private List<KyyyWritingFacetResponse> buildFacetResponses(List<String> orderedCodes,
                                                               Map<String, Long> counter,
                                                               Map<String, String> labels,
                                                               String essaySection) {
        List<KyyyWritingFacetResponse> responses = new ArrayList<>();
        orderedCodes.forEach(code -> {
            long count = counter.getOrDefault(code, 0L);
            if (count > 0) {
                responses.add(new KyyyWritingFacetResponse(
                        code,
                        labels.getOrDefault(code, code),
                        count,
                        essaySection
                ));
            }
        });
        return responses;
    }

    private List<KyyyWritingFacetResponse> buildPromptCategoryFacets(Map<String, Long> counter) {
        List<KyyyWritingFacetResponse> responses = new ArrayList<>();
        List<String> orderedSmallCategories = List.of("email", "letter", "notice", "memo", "report", "practical_writing");
        List<String> orderedBigCategories = List.of("chart", "picture", "essay");
        orderedSmallCategories.forEach(category -> appendPromptCategoryFacet(counter, responses, "small", category));
        orderedBigCategories.forEach(category -> appendPromptCategoryFacet(counter, responses, "big", category));
        return responses;
    }

    private void appendPromptCategoryFacet(Map<String, Long> counter,
                                           List<KyyyWritingFacetResponse> responses,
                                           String essaySection,
                                           String promptCategory) {
        String compositeKey = essaySection + ":" + promptCategory;
        long count = counter.getOrDefault(compositeKey, 0L);
        if (count <= 0) {
            return;
        }
        responses.add(new KyyyWritingFacetResponse(
                promptCategory,
                PROMPT_CATEGORY_LABELS.getOrDefault(promptCategory, promptCategory),
                count,
                essaySection
        ));
    }

    private KyyyWritingEssayCardResponse toEssayCard(KyyyWritingEssay essay) {
        return new KyyyWritingEssayCardResponse(
                essay.getId(),
                essay.getWritingCode(),
                essay.getSourceTitle(),
                essay.getExamDirection(),
                essay.getSourceYear(),
                essay.getEssaySection(),
                essay.getPromptCategory(),
                essay.getScoreValue(),
                essay.getWordLimitMin(),
                essay.getWordLimitMax(),
                splitTags(essay.getKnowledgeTags())
        );
    }

    private List<String> splitTags(String rawTags) {
        if (!StringUtils.hasText(rawTags)) {
            return List.of();
        }
        return Arrays.stream(rawTags.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), ArrayList::new));
    }

    private String normalizeExamDirection(String examDirection) {
        if (!StringUtils.hasText(examDirection)) {
            return null;
        }
        String normalized = examDirection.trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_EXAM_DIRECTIONS.contains(normalized)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "考试方向参数不支持");
        }
        return normalized;
    }

    private String normalizeEssaySection(String essaySection) {
        if (!StringUtils.hasText(essaySection)) {
            return null;
        }
        String normalized = essaySection.trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_ESSAY_SECTIONS.contains(normalized)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "作文分区参数不支持");
        }
        return normalized;
    }

    private String normalizePromptCategory(String promptCategory) {
        if (!StringUtils.hasText(promptCategory)) {
            return null;
        }
        String normalized = promptCategory.trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_PROMPT_CATEGORIES.contains(normalized)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "作文题型参数不支持");
        }
        return normalized;
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return keyword.trim();
    }
}
