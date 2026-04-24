package org.example.backend.biz.kyzz.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.backend.biz.kyzz.admin.dto.KyzzCategoryOptionResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionAdminDashboardResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionAdminDetailResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionAdminItemResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionAdminOptionRequest;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionAdminOptionResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionAdminPaginationResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionAdminStatsResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionAdminUpsertRequest;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionBankOptionResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionStatusUpdateRequest;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionTagOptionResponse;
import org.example.backend.biz.kyzz.admin.dto.KyzzQuestionTagSimpleResponse;
import org.example.backend.biz.kyzz.admin.support.KyzzAdminAccessSupport;
import org.example.backend.biz.kyzz.entity.KyzzCategory;
import org.example.backend.biz.kyzz.entity.KyzzComment;
import org.example.backend.biz.kyzz.entity.KyzzQuestion;
import org.example.backend.biz.kyzz.entity.KyzzQuestionBank;
import org.example.backend.biz.kyzz.entity.KyzzQuestionOption;
import org.example.backend.biz.kyzz.entity.KyzzQuestionTagRel;
import org.example.backend.biz.kyzz.entity.KyzzTag;
import org.example.backend.biz.kyzz.entity.KyzzUserAnswer;
import org.example.backend.biz.kyzz.entity.KyzzUserNote;
import org.example.backend.biz.kyzz.entity.KyzzUserWrongQuestion;
import org.example.backend.biz.kyzz.mapper.KyzzCategoryMapper;
import org.example.backend.biz.kyzz.mapper.KyzzCommentMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionBankMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionOptionMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionTagRelMapper;
import org.example.backend.biz.kyzz.mapper.KyzzTagMapper;
import org.example.backend.biz.kyzz.mapper.KyzzUserAnswerMapper;
import org.example.backend.biz.kyzz.mapper.KyzzUserNoteMapper;
import org.example.backend.biz.kyzz.mapper.KyzzUserWrongQuestionMapper;
import org.example.backend.biz.kyzz.support.KyzzCacheService;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * AI 索引: KYZZ 题目管理后台服务。
 */
@Service
public class KyzzQuestionAdminService {

    private static final String QUESTION_TYPE_SINGLE = "single";
    private static final String QUESTION_TYPE_MULTIPLE = "multiple";
    private static final String QUESTION_TYPE_SHORT = "short";
    private static final String QUESTION_TAG_TYPE = "question";
    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MIN_OPTION_COUNT = 2;
    private static final int MAX_OPTION_COUNT = 8;
    private static final int STEM_PREVIEW_LIMIT = 68;

    private final KyzzQuestionMapper kyzzQuestionMapper;
    private final KyzzQuestionOptionMapper kyzzQuestionOptionMapper;
    private final KyzzQuestionBankMapper kyzzQuestionBankMapper;
    private final KyzzCategoryMapper kyzzCategoryMapper;
    private final KyzzTagMapper kyzzTagMapper;
    private final KyzzUserAnswerMapper kyzzUserAnswerMapper;
    private final KyzzUserWrongQuestionMapper kyzzUserWrongQuestionMapper;
    private final KyzzUserNoteMapper kyzzUserNoteMapper;
    private final KyzzCommentMapper kyzzCommentMapper;
    private final KyzzQuestionTagRelMapper kyzzQuestionTagRelMapper;
    private final KyzzAdminAccessSupport kyzzAdminAccessSupport;
    private final KyzzQuestionBankAdminService kyzzQuestionBankAdminService;
    private final KyzzQuestionTagAdminService kyzzQuestionTagAdminService;
    private final KyzzCacheService kyzzCacheService;

    public KyzzQuestionAdminService(KyzzQuestionMapper kyzzQuestionMapper,
                                    KyzzQuestionOptionMapper kyzzQuestionOptionMapper,
                                    KyzzQuestionBankMapper kyzzQuestionBankMapper,
                                    KyzzCategoryMapper kyzzCategoryMapper,
                                    KyzzTagMapper kyzzTagMapper,
                                    KyzzUserAnswerMapper kyzzUserAnswerMapper,
                                    KyzzUserWrongQuestionMapper kyzzUserWrongQuestionMapper,
                                    KyzzUserNoteMapper kyzzUserNoteMapper,
                                    KyzzCommentMapper kyzzCommentMapper,
                                    KyzzQuestionTagRelMapper kyzzQuestionTagRelMapper,
                                    KyzzAdminAccessSupport kyzzAdminAccessSupport,
                                    KyzzQuestionBankAdminService kyzzQuestionBankAdminService,
                                    KyzzQuestionTagAdminService kyzzQuestionTagAdminService,
                                    KyzzCacheService kyzzCacheService) {
        this.kyzzQuestionMapper = kyzzQuestionMapper;
        this.kyzzQuestionOptionMapper = kyzzQuestionOptionMapper;
        this.kyzzQuestionBankMapper = kyzzQuestionBankMapper;
        this.kyzzCategoryMapper = kyzzCategoryMapper;
        this.kyzzTagMapper = kyzzTagMapper;
        this.kyzzUserAnswerMapper = kyzzUserAnswerMapper;
        this.kyzzUserWrongQuestionMapper = kyzzUserWrongQuestionMapper;
        this.kyzzUserNoteMapper = kyzzUserNoteMapper;
        this.kyzzCommentMapper = kyzzCommentMapper;
        this.kyzzQuestionTagRelMapper = kyzzQuestionTagRelMapper;
        this.kyzzAdminAccessSupport = kyzzAdminAccessSupport;
        this.kyzzQuestionBankAdminService = kyzzQuestionBankAdminService;
        this.kyzzQuestionTagAdminService = kyzzQuestionTagAdminService;
        this.kyzzCacheService = kyzzCacheService;
    }

    public KyzzQuestionAdminDashboardResponse getDashboard(Long operatorId,
                                                           Long pageNo,
                                                           Long pageSize,
                                                           String keyword,
                                                           Long questionBankId,
                                                           Long categoryId,
                                                           Long tagId,
                                                           String questionType,
                                                           Integer status,
                                                           Integer difficultyLevel,
                                                           Integer yearNo) {
        kyzzAdminAccessSupport.requireProjectAccess(operatorId);

        QueryFilter filter = normalizeFilter(pageNo, pageSize, keyword, questionBankId, categoryId, tagId, questionType, status, difficultyLevel, yearNo);
        List<KyzzQuestionBank> allBanks = loadAllBanks();
        List<KyzzCategory> allCategories = loadAllCategories();
        Map<Long, KyzzQuestionBank> questionBankMap = buildQuestionBankMap(allBanks);
        Map<Long, KyzzCategory> categoryMap = buildCategoryMap(allCategories);

        Page<KyzzQuestion> page = new Page<>(filter.pageNo(), filter.pageSize());
        Page<KyzzQuestion> resultPage = kyzzQuestionMapper.selectPage(page, applyFilters(new LambdaQueryWrapper<KyzzQuestion>(), filter)
                .orderByAsc(KyzzQuestion::getSortNo)
                .orderByDesc(KyzzQuestion::getId));

        List<KyzzQuestion> questions = resultPage.getRecords();
        List<Long> questionIds = questions.stream().map(KyzzQuestion::getId).toList();
        Map<Long, List<KyzzQuestionOption>> optionMap = loadOptionMap(questionIds);
        Map<Long, List<KyzzQuestionTagSimpleResponse>> tagMap = loadQuestionTagMap(questionIds);
        Map<Long, String> deleteReasonMap = buildDeleteReasonMap(questionIds);

        List<KyzzQuestionAdminItemResponse> records = questions.stream()
                .map(question -> toItem(question, questionBankMap, categoryMap, optionMap, tagMap, deleteReasonMap))
                .toList();

        List<KyzzQuestionBankOptionResponse> questionBanks = allBanks.stream()
                .map(bank -> new KyzzQuestionBankOptionResponse(
                        bank.getId(),
                        bank.getBankCode(),
                        bank.getBankName(),
                        bank.getCategoryId(),
                        bank.getCategoryId() == null ? null : categoryMap.get(bank.getCategoryId()) == null ? null : categoryMap.get(bank.getCategoryId()).getCategoryName(),
                        bank.getStatus()
                ))
                .toList();
        List<KyzzCategoryOptionResponse> categories = allCategories.stream()
                .map(category -> new KyzzCategoryOptionResponse(
                        category.getId(),
                        category.getCategoryCode(),
                        category.getCategoryName(),
                        category.getCategoryLevel(),
                        category.getIsEnabled()
                ))
                .toList();
        List<KyzzQuestionTagOptionResponse> tags = kyzzQuestionTagAdminService.listQuestionTagOptions();

        return new KyzzQuestionAdminDashboardResponse(
                buildStats(filter),
                records,
                new KyzzQuestionAdminPaginationResponse(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal(), resultPage.getPages()),
                questionBanks,
                categories,
                tags
        );
    }

    public KyzzQuestionAdminDetailResponse getQuestionDetail(Long operatorId, Long questionId) {
        kyzzAdminAccessSupport.requireProjectAccess(operatorId);
        return requireDetail(questionId);
    }

    @Transactional
    public KyzzQuestionAdminDetailResponse createQuestion(Long operatorId, KyzzQuestionAdminUpsertRequest request) {
        kyzzAdminAccessSupport.requireProjectAccess(operatorId);
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题目参数不能为空");
        }

        NormalizedQuestionPayload payload = normalizePayload(request, null);
        KyzzQuestion question = new KyzzQuestion();
        question.setQuestionBankId(payload.questionBankId());
        question.setCategoryId(payload.categoryId());
        question.setQuestionType(payload.questionType());
        question.setStem(payload.stem());
        question.setAnalysis(payload.analysis());
        question.setAnswerText(payload.answerText());
        question.setDifficultyLevel(payload.difficultyLevel());
        question.setScore(payload.score());
        question.setSourceName(payload.sourceName());
        question.setYearNo(payload.yearNo());
        question.setSortNo(payload.sortNo());
        question.setStatus(payload.status());
        kyzzQuestionMapper.insert(question);

        replaceQuestionOptions(question.getId(), payload);
        replaceQuestionTags(question.getId(), List.of(), payload.tagIds());
        kyzzQuestionBankAdminService.syncQuestionCounts(List.of(payload.questionBankId()));
        kyzzCacheService.evictPublicBaseCaches();
        return requireDetail(question.getId());
    }

    @Transactional
    public KyzzQuestionAdminDetailResponse updateQuestion(Long operatorId, Long questionId, KyzzQuestionAdminUpsertRequest request) {
        kyzzAdminAccessSupport.requireProjectAccess(operatorId);
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题目参数不能为空");
        }

        KyzzQuestion existing = requireQuestion(questionId);
        List<Long> previousTagIds = loadQuestionTagIds(questionId);
        NormalizedQuestionPayload payload = normalizePayload(request, existing);
        Long previousBankId = existing.getQuestionBankId();

        kyzzQuestionMapper.update(null, new LambdaUpdateWrapper<KyzzQuestion>()
                .eq(KyzzQuestion::getId, questionId)
                .set(KyzzQuestion::getQuestionBankId, payload.questionBankId())
                .set(KyzzQuestion::getCategoryId, payload.categoryId())
                .set(KyzzQuestion::getQuestionType, payload.questionType())
                .set(KyzzQuestion::getStem, payload.stem())
                .set(KyzzQuestion::getAnalysis, payload.analysis())
                .set(KyzzQuestion::getAnswerText, payload.answerText())
                .set(KyzzQuestion::getDifficultyLevel, payload.difficultyLevel())
                .set(KyzzQuestion::getScore, payload.score())
                .set(KyzzQuestion::getSourceName, payload.sourceName())
                .set(KyzzQuestion::getYearNo, payload.yearNo())
                .set(KyzzQuestion::getSortNo, payload.sortNo())
                .set(KyzzQuestion::getStatus, payload.status()));

        replaceQuestionOptions(questionId, payload);
        replaceQuestionTags(questionId, previousTagIds, payload.tagIds());
        kyzzQuestionBankAdminService.syncQuestionCounts(List.of(previousBankId, payload.questionBankId()));
        kyzzCacheService.evictPublicBaseCaches();
        kyzzCacheService.evictQuestionCommentCaches(questionId);
        return requireDetail(questionId);
    }

    @Transactional
    public KyzzQuestionAdminItemResponse updateQuestionStatus(Long operatorId,
                                                              Long questionId,
                                                              KyzzQuestionStatusUpdateRequest request) {
        kyzzAdminAccessSupport.requireProjectAccess(operatorId);
        if (request == null || request.getStatus() == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题目状态不能为空");
        }
        validateStatus(request.getStatus(), false);

        requireQuestion(questionId);
        kyzzQuestionMapper.update(null, new LambdaUpdateWrapper<KyzzQuestion>()
                .eq(KyzzQuestion::getId, questionId)
                .set(KyzzQuestion::getStatus, request.getStatus()));
        kyzzCacheService.evictPublicBaseCaches();
        return requireItem(questionId);
    }

    @Transactional
    public void deleteQuestion(Long operatorId, Long questionId) {
        kyzzAdminAccessSupport.requireProjectAccess(operatorId);
        KyzzQuestion question = requireQuestion(questionId);

        String deleteBlockReason = buildDeleteReasonMap(List.of(questionId)).get(questionId);
        if (StringUtils.hasText(deleteBlockReason)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, deleteBlockReason);
        }

        List<Long> previousTagIds = loadQuestionTagIds(questionId);
        kyzzQuestionOptionMapper.delete(new LambdaQueryWrapper<KyzzQuestionOption>()
                .eq(KyzzQuestionOption::getQuestionId, questionId));
        kyzzQuestionTagRelMapper.delete(new LambdaQueryWrapper<KyzzQuestionTagRel>()
                .eq(KyzzQuestionTagRel::getQuestionId, questionId));
        kyzzQuestionMapper.deleteById(questionId);
        kyzzQuestionTagAdminService.syncUseCounts(previousTagIds);
        kyzzQuestionBankAdminService.syncQuestionCounts(List.of(question.getQuestionBankId()));
        kyzzCacheService.evictPublicBaseCaches();
        kyzzCacheService.evictQuestionCommentCaches(questionId);
    }

    private KyzzQuestionAdminItemResponse requireItem(Long questionId) {
        KyzzQuestion question = requireQuestion(questionId);
        Map<Long, KyzzQuestionBank> questionBankMap = buildQuestionBankMap(List.of(requireQuestionBank(question.getQuestionBankId())));
        Map<Long, KyzzCategory> categoryMap = question.getCategoryId() == null
                ? Map.of()
                : buildCategoryMap(List.of(requireCategory(question.getCategoryId())));
        Map<Long, List<KyzzQuestionOption>> optionMap = loadOptionMap(List.of(questionId));
        Map<Long, List<KyzzQuestionTagSimpleResponse>> tagMap = loadQuestionTagMap(List.of(questionId));
        Map<Long, String> deleteReasonMap = buildDeleteReasonMap(List.of(questionId));
        return toItem(question, questionBankMap, categoryMap, optionMap, tagMap, deleteReasonMap);
    }

    private KyzzQuestionAdminDetailResponse requireDetail(Long questionId) {
        KyzzQuestion question = requireQuestion(questionId);
        Map<Long, KyzzQuestionBank> questionBankMap = buildQuestionBankMap(List.of(requireQuestionBank(question.getQuestionBankId())));
        Map<Long, KyzzCategory> categoryMap = question.getCategoryId() == null
                ? Map.of()
                : buildCategoryMap(List.of(requireCategory(question.getCategoryId())));
        Map<Long, List<KyzzQuestionOption>> optionMap = loadOptionMap(List.of(questionId));
        Map<Long, List<KyzzQuestionTagSimpleResponse>> tagMap = loadQuestionTagMap(List.of(questionId));
        Map<Long, String> deleteReasonMap = buildDeleteReasonMap(List.of(questionId));
        return toDetail(question, questionBankMap, categoryMap, optionMap, tagMap, deleteReasonMap);
    }

    private List<KyzzQuestionBank> loadAllBanks() {
        return kyzzQuestionBankMapper.selectList(new LambdaQueryWrapper<KyzzQuestionBank>()
                .orderByAsc(KyzzQuestionBank::getSortNo)
                .orderByDesc(KyzzQuestionBank::getId));
    }

    private List<KyzzCategory> loadAllCategories() {
        return kyzzCategoryMapper.selectList(new LambdaQueryWrapper<KyzzCategory>()
                .orderByAsc(KyzzCategory::getCategoryLevel)
                .orderByAsc(KyzzCategory::getSortNo)
                .orderByAsc(KyzzCategory::getId));
    }

    private Map<Long, KyzzQuestionBank> buildQuestionBankMap(List<KyzzQuestionBank> banks) {
        Map<Long, KyzzQuestionBank> result = new HashMap<>();
        banks.forEach(bank -> result.put(bank.getId(), bank));
        return result;
    }

    private Map<Long, KyzzCategory> buildCategoryMap(List<KyzzCategory> categories) {
        Map<Long, KyzzCategory> result = new HashMap<>();
        categories.forEach(category -> result.put(category.getId(), category));
        return result;
    }

    private QueryFilter normalizeFilter(Long pageNo,
                                        Long pageSize,
                                        String keyword,
                                        Long questionBankId,
                                        Long categoryId,
                                        Long tagId,
                                        String questionType,
                                        Integer status,
                                        Integer difficultyLevel,
                                        Integer yearNo) {
        long normalizedPageNo = pageNo == null || pageNo < DEFAULT_PAGE_NO ? DEFAULT_PAGE_NO : pageNo;
        long normalizedPageSize = pageSize == null || pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        String normalizedQuestionType = normalizeQuestionType(questionType, true);
        validateStatus(status, true);
        validateDifficultyLevel(difficultyLevel, true);
        validateYearNo(yearNo, true);
        return new QueryFilter(
                normalizedPageNo,
                normalizedPageSize,
                trimToNull(keyword),
                questionBankId,
                categoryId,
                tagId,
                normalizedQuestionType,
                status,
                difficultyLevel,
                yearNo
        );
    }

    private LambdaQueryWrapper<KyzzQuestion> applyFilters(LambdaQueryWrapper<KyzzQuestion> wrapper, QueryFilter filter) {
        if (StringUtils.hasText(filter.keyword())) {
            String keyword = filter.keyword().trim();
            wrapper.and(query -> query.like(KyzzQuestion::getStem, keyword)
                    .or()
                    .like(KyzzQuestion::getSourceName, keyword)
                    .or()
                    .like(KyzzQuestion::getAnswerText, keyword));
        }
        if (filter.questionBankId() != null) {
            wrapper.eq(KyzzQuestion::getQuestionBankId, filter.questionBankId());
        }
        if (filter.categoryId() != null) {
            wrapper.eq(KyzzQuestion::getCategoryId, filter.categoryId());
        }
        if (filter.tagId() != null) {
            wrapper.inSql(KyzzQuestion::getId, "select question_id from kyzz_question_tag_rel where tag_id = " + filter.tagId());
        }
        if (StringUtils.hasText(filter.questionType())) {
            wrapper.eq(KyzzQuestion::getQuestionType, filter.questionType());
        }
        if (filter.status() != null) {
            wrapper.eq(KyzzQuestion::getStatus, filter.status());
        }
        if (filter.difficultyLevel() != null) {
            wrapper.eq(KyzzQuestion::getDifficultyLevel, filter.difficultyLevel());
        }
        if (filter.yearNo() != null) {
            wrapper.eq(KyzzQuestion::getYearNo, filter.yearNo());
        }
        return wrapper;
    }

    private KyzzQuestionAdminStatsResponse buildStats(QueryFilter filter) {
        return new KyzzQuestionAdminStatsResponse(
                countQuestions(filter, null),
                countQuestions(filter, wrapper -> wrapper.eq(KyzzQuestion::getStatus, 1)),
                countQuestions(filter, wrapper -> wrapper.eq(KyzzQuestion::getStatus, 0)),
                countQuestions(filter, wrapper -> wrapper.eq(KyzzQuestion::getQuestionType, QUESTION_TYPE_SINGLE)),
                countQuestions(filter, wrapper -> wrapper.eq(KyzzQuestion::getQuestionType, QUESTION_TYPE_MULTIPLE)),
                countQuestions(filter, wrapper -> wrapper.eq(KyzzQuestion::getQuestionType, QUESTION_TYPE_SHORT))
        );
    }

    private int countQuestions(QueryFilter filter, Consumer<LambdaQueryWrapper<KyzzQuestion>> extraCondition) {
        LambdaQueryWrapper<KyzzQuestion> wrapper = applyFilters(new LambdaQueryWrapper<>(), filter);
        if (extraCondition != null) {
            extraCondition.accept(wrapper);
        }
        Long count = kyzzQuestionMapper.selectCount(wrapper);
        return count == null ? 0 : count.intValue();
    }

    private NormalizedQuestionPayload normalizePayload(KyzzQuestionAdminUpsertRequest request, KyzzQuestion existing) {
        KyzzQuestionBank questionBank = requireQuestionBank(request.getQuestionBankId());
        String questionType = normalizeQuestionType(request.getQuestionType(), false);
        String stem = normalizeRequiredText(request.getStem(), 20000, "题干不能为空", "题干不能超过 20000 个字符");
        String analysis = normalizeNullableText(request.getAnalysis(), 20000, "题目解析不能超过 20000 个字符");
        Integer difficultyLevel = request.getDifficultyLevel() == null ? 2 : request.getDifficultyLevel();
        BigDecimal score = normalizeScore(request.getScore());
        String sourceName = normalizeNullableText(request.getSourceName(), 100, "题目来源不能超过 100 个字符");
        Integer yearNo = request.getYearNo();
        Integer sortNo = request.getSortNo() == null ? 0 : request.getSortNo();
        Integer status = request.getStatus() == null ? 1 : request.getStatus();
        List<Long> tagIds = kyzzQuestionTagAdminService.normalizeQuestionTagIds(request.getTagIds());

        validateDifficultyLevel(difficultyLevel, false);
        validateYearNo(yearNo, true);
        validateSortNo(sortNo);
        validateStatus(status, false);

        Long categoryId = resolveCategoryId(request.getCategoryId(), questionBank, existing);
        List<NormalizedOptionPayload> options = normalizeOptions(questionType, request.getOptions());
        String answerText = QUESTION_TYPE_SHORT.equals(questionType)
                ? normalizeRequiredText(request.getAnswerText(), 20000, "简答题标准答案不能为空", "标准答案不能超过 20000 个字符")
                : buildObjectiveAnswerText(options);

        return new NormalizedQuestionPayload(
                questionBank.getId(),
                categoryId,
                questionType,
                stem,
                analysis,
                answerText,
                difficultyLevel,
                score,
                sourceName,
                yearNo,
                sortNo,
                status,
                tagIds,
                options
        );
    }

    private Long resolveCategoryId(Long incomingCategoryId, KyzzQuestionBank questionBank, KyzzQuestion existing) {
        Long resolvedCategoryId = incomingCategoryId != null ? incomingCategoryId : questionBank.getCategoryId();
        if (resolvedCategoryId == null) {
            return null;
        }
        validateCategoryId(resolvedCategoryId, existing == null ? null : existing.getCategoryId());
        return resolvedCategoryId;
    }

    private List<NormalizedOptionPayload> normalizeOptions(String questionType, List<KyzzQuestionAdminOptionRequest> options) {
        if (QUESTION_TYPE_SHORT.equals(questionType)) {
            if (options != null && !options.isEmpty()) {
                throw new BusinessException(ApiResponseCode.BAD_REQUEST, "简答题不支持保存选择项");
            }
            return List.of();
        }

        if (options == null || options.size() < MIN_OPTION_COUNT || options.size() > MAX_OPTION_COUNT) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "客观题需保持 2 到 8 个选项");
        }

        List<NormalizedOptionPayload> normalizedOptions = new ArrayList<>();
        int correctCount = 0;
        for (int i = 0; i < options.size(); i++) {
            KyzzQuestionAdminOptionRequest option = options.get(i);
            String optionContent = normalizeRequiredText(option == null ? null : option.getOptionContent(), 10000, "选项内容不能为空", "选项内容不能超过 10000 个字符");
            int isCorrect = option != null && Objects.equals(option.getIsCorrect(), 1) ? 1 : 0;
            if (isCorrect == 1) {
                correctCount++;
            }
            normalizedOptions.add(new NormalizedOptionPayload(String.valueOf((char) ('A' + i)), optionContent, isCorrect, i));
        }

        if (QUESTION_TYPE_SINGLE.equals(questionType) && correctCount != 1) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "单选题必须且只能设置 1 个正确答案");
        }
        if (QUESTION_TYPE_MULTIPLE.equals(questionType) && correctCount < 2) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "多选题至少需要设置 2 个正确答案");
        }
        return normalizedOptions;
    }

    private void replaceQuestionOptions(Long questionId, NormalizedQuestionPayload payload) {
        kyzzQuestionOptionMapper.delete(new LambdaQueryWrapper<KyzzQuestionOption>()
                .eq(KyzzQuestionOption::getQuestionId, questionId));
        if (QUESTION_TYPE_SHORT.equals(payload.questionType())) {
            return;
        }

        payload.options().forEach(option -> {
            KyzzQuestionOption entity = new KyzzQuestionOption();
            entity.setQuestionId(questionId);
            entity.setOptionKey(option.optionKey());
            entity.setOptionContent(option.optionContent());
            entity.setIsCorrect(option.isCorrect());
            entity.setSortNo(option.sortNo());
            kyzzQuestionOptionMapper.insert(entity);
        });
    }

    private void replaceQuestionTags(Long questionId, Collection<Long> previousTagIds, List<Long> nextTagIds) {
        kyzzQuestionTagRelMapper.delete(new LambdaQueryWrapper<KyzzQuestionTagRel>()
                .eq(KyzzQuestionTagRel::getQuestionId, questionId));
        nextTagIds.forEach(tagId -> {
            KyzzQuestionTagRel relation = new KyzzQuestionTagRel();
            relation.setQuestionId(questionId);
            relation.setTagId(tagId);
            kyzzQuestionTagRelMapper.insert(relation);
        });

        LinkedHashSet<Long> affectedTagIds = new LinkedHashSet<>();
        if (previousTagIds != null) {
            affectedTagIds.addAll(previousTagIds);
        }
        affectedTagIds.addAll(nextTagIds);
        kyzzQuestionTagAdminService.syncUseCounts(affectedTagIds);
    }

    private String buildObjectiveAnswerText(List<NormalizedOptionPayload> options) {
        return options.stream()
                .filter(option -> option.isCorrect() == 1)
                .sorted(Comparator.comparing(NormalizedOptionPayload::sortNo))
                .map(NormalizedOptionPayload::optionKey)
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    private Map<Long, List<KyzzQuestionOption>> loadOptionMap(List<Long> questionIds) {
        LinkedHashSet<Long> normalizedIds = questionIds.stream()
                .filter(Objects::nonNull)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
        if (normalizedIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<KyzzQuestionOption>> result = new HashMap<>();
        kyzzQuestionOptionMapper.selectList(new LambdaQueryWrapper<KyzzQuestionOption>()
                        .in(KyzzQuestionOption::getQuestionId, normalizedIds)
                        .orderByAsc(KyzzQuestionOption::getQuestionId)
                        .orderByAsc(KyzzQuestionOption::getSortNo)
                        .orderByAsc(KyzzQuestionOption::getId))
                .forEach(option -> result.computeIfAbsent(option.getQuestionId(), key -> new ArrayList<>()).add(option));
        return result;
    }

    private Map<Long, List<KyzzQuestionTagSimpleResponse>> loadQuestionTagMap(List<Long> questionIds) {
        LinkedHashSet<Long> normalizedIds = questionIds.stream()
                .filter(Objects::nonNull)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
        if (normalizedIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<Long>> questionTagIdsMap = new HashMap<>();
        LinkedHashSet<Long> tagIds = new LinkedHashSet<>();
        kyzzQuestionTagRelMapper.selectList(new LambdaQueryWrapper<KyzzQuestionTagRel>()
                        .in(KyzzQuestionTagRel::getQuestionId, normalizedIds)
                        .orderByAsc(KyzzQuestionTagRel::getQuestionId)
                        .orderByAsc(KyzzQuestionTagRel::getId))
                .forEach(relation -> {
                    questionTagIdsMap.computeIfAbsent(relation.getQuestionId(), key -> new ArrayList<>()).add(relation.getTagId());
                    tagIds.add(relation.getTagId());
                });
        if (tagIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, KyzzTag> tagEntityMap = new HashMap<>();
        kyzzTagMapper.selectList(new LambdaQueryWrapper<KyzzTag>()
                        .in(KyzzTag::getId, tagIds)
                        .eq(KyzzTag::getTagType, QUESTION_TAG_TYPE))
                .forEach(tag -> tagEntityMap.put(tag.getId(), tag));

        Map<Long, List<KyzzQuestionTagSimpleResponse>> result = new HashMap<>();
        questionTagIdsMap.forEach((questionId, relatedTagIds) -> {
            List<KyzzQuestionTagSimpleResponse> tags = new ArrayList<>();
            relatedTagIds.forEach(tagId -> {
                KyzzTag tag = tagEntityMap.get(tagId);
                if (tag != null) {
                    tags.add(new KyzzQuestionTagSimpleResponse(tag.getId(), tag.getTagName(), tag.getColor()));
                }
            });
            result.put(questionId, tags);
        });
        return result;
    }

    private List<Long> loadQuestionTagIds(Long questionId) {
        return kyzzQuestionTagRelMapper.selectList(new LambdaQueryWrapper<KyzzQuestionTagRel>()
                        .eq(KyzzQuestionTagRel::getQuestionId, questionId)
                        .orderByAsc(KyzzQuestionTagRel::getId))
                .stream()
                .map(KyzzQuestionTagRel::getTagId)
                .toList();
    }

    private Map<Long, String> buildDeleteReasonMap(List<Long> questionIds) {
        LinkedHashSet<Long> normalizedIds = questionIds.stream()
                .filter(Objects::nonNull)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
        if (normalizedIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Integer> answerCountMap = loadRelationCountMap(kyzzUserAnswerMapper.selectMaps(
                new QueryWrapper<KyzzUserAnswer>()
                        .select("question_id AS relationId", "COUNT(*) AS relationCount")
                        .in("question_id", normalizedIds)
                        .groupBy("question_id")));
        Map<Long, Integer> wrongCountMap = loadRelationCountMap(kyzzUserWrongQuestionMapper.selectMaps(
                new QueryWrapper<KyzzUserWrongQuestion>()
                        .select("question_id AS relationId", "COUNT(*) AS relationCount")
                        .in("question_id", normalizedIds)
                        .groupBy("question_id")));
        Map<Long, Integer> noteCountMap = loadRelationCountMap(kyzzUserNoteMapper.selectMaps(
                new QueryWrapper<KyzzUserNote>()
                        .select("question_id AS relationId", "COUNT(*) AS relationCount")
                        .in("question_id", normalizedIds)
                        .groupBy("question_id")));
        Map<Long, Integer> commentCountMap = loadRelationCountMap(kyzzCommentMapper.selectMaps(
                new QueryWrapper<KyzzComment>()
                        .select("target_id AS relationId", "COUNT(*) AS relationCount")
                        .eq("target_type", "question")
                        .in("target_id", normalizedIds)
                        .groupBy("target_id")));

        Map<Long, String> result = new HashMap<>();
        normalizedIds.forEach(questionId -> {
            String reason = buildDeleteBlockReason(
                    answerCountMap.getOrDefault(questionId, 0),
                    wrongCountMap.getOrDefault(questionId, 0),
                    noteCountMap.getOrDefault(questionId, 0),
                    commentCountMap.getOrDefault(questionId, 0)
            );
            if (StringUtils.hasText(reason)) {
                result.put(questionId, reason);
            }
        });
        return result;
    }

    private String buildDeleteBlockReason(int answerCount, int wrongCount, int noteCount, int commentCount) {
        List<String> segments = new ArrayList<>();
        if (answerCount > 0) {
            segments.add("答题记录 " + answerCount + " 条");
        }
        if (wrongCount > 0) {
            segments.add("错题记录 " + wrongCount + " 条");
        }
        if (noteCount > 0) {
            segments.add("笔记 " + noteCount + " 条");
        }
        if (commentCount > 0) {
            segments.add("评论 " + commentCount + " 条");
        }
        if (segments.isEmpty()) {
            return null;
        }
        return "题目已产生" + String.join("、", segments) + "，请先停用，不支持直接删除";
    }

    private Map<Long, Integer> loadRelationCountMap(List<Map<String, Object>> rows) {
        Map<Long, Integer> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object relationId = row.get("relationId");
            Object relationCount = row.get("relationCount");
            if (relationId instanceof Number id && relationCount instanceof Number count) {
                result.put(id.longValue(), count.intValue());
            }
        }
        return result;
    }

    private KyzzQuestionAdminItemResponse toItem(KyzzQuestion question,
                                                 Map<Long, KyzzQuestionBank> questionBankMap,
                                                 Map<Long, KyzzCategory> categoryMap,
                                                 Map<Long, List<KyzzQuestionOption>> optionMap,
                                                 Map<Long, List<KyzzQuestionTagSimpleResponse>> tagMap,
                                                 Map<Long, String> deleteReasonMap) {
        KyzzQuestionBank questionBank = questionBankMap.get(question.getQuestionBankId());
        KyzzCategory category = question.getCategoryId() == null ? null : categoryMap.get(question.getCategoryId());
        List<KyzzQuestionOption> options = optionMap.getOrDefault(question.getId(), List.of());
        List<KyzzQuestionTagSimpleResponse> tags = tagMap.getOrDefault(question.getId(), List.of());
        List<String> correctOptionKeys = options.stream()
                .filter(option -> Objects.equals(option.getIsCorrect(), 1))
                .sorted(Comparator.comparing(KyzzQuestionOption::getSortNo, Comparator.nullsFirst(Integer::compareTo))
                        .thenComparing(KyzzQuestionOption::getId, Comparator.nullsLast(Long::compareTo)))
                .map(KyzzQuestionOption::getOptionKey)
                .toList();
        String deleteReason = deleteReasonMap.get(question.getId());
        return new KyzzQuestionAdminItemResponse(
                question.getId(),
                question.getQuestionBankId(),
                questionBank == null ? null : questionBank.getBankName(),
                question.getCategoryId(),
                category == null ? null : category.getCategoryName(),
                question.getQuestionType(),
                question.getDifficultyLevel(),
                question.getScore() == null ? BigDecimal.ZERO.setScale(2) : question.getScore(),
                question.getSourceName(),
                question.getYearNo(),
                question.getSortNo(),
                question.getStatus(),
                question.getStem(),
                buildPreview(question.getStem()),
                question.getAnalysis(),
                question.getAnswerText(),
                tags,
                correctOptionKeys,
                options.size(),
                !StringUtils.hasText(deleteReason),
                deleteReason,
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }

    private KyzzQuestionAdminDetailResponse toDetail(KyzzQuestion question,
                                                     Map<Long, KyzzQuestionBank> questionBankMap,
                                                     Map<Long, KyzzCategory> categoryMap,
                                                     Map<Long, List<KyzzQuestionOption>> optionMap,
                                                     Map<Long, List<KyzzQuestionTagSimpleResponse>> tagMap,
                                                     Map<Long, String> deleteReasonMap) {
        KyzzQuestionAdminItemResponse item = toItem(question, questionBankMap, categoryMap, optionMap, tagMap, deleteReasonMap);
        List<KyzzQuestionAdminOptionResponse> options = optionMap.getOrDefault(question.getId(), List.of()).stream()
                .map(option -> new KyzzQuestionAdminOptionResponse(
                        option.getId(),
                        option.getOptionKey(),
                        option.getOptionContent(),
                        option.getIsCorrect(),
                        option.getSortNo()
                ))
                .toList();
        return new KyzzQuestionAdminDetailResponse(
                item.getId(),
                item.getQuestionBankId(),
                item.getQuestionBankName(),
                item.getCategoryId(),
                item.getCategoryName(),
                item.getQuestionType(),
                item.getDifficultyLevel(),
                item.getScore(),
                item.getSourceName(),
                item.getYearNo(),
                item.getSortNo(),
                item.getStatus(),
                item.getStem(),
                item.getStemPreview(),
                item.getAnalysis(),
                item.getAnswerText(),
                item.getTags(),
                item.getCorrectOptionKeys(),
                item.getOptionCount(),
                item.getCanDelete(),
                item.getDeleteBlockReason(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                options
        );
    }

    private String buildPreview(String value) {
        if (!StringUtils.hasText(value)) {
            return "未填写题干";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= STEM_PREVIEW_LIMIT) {
            return normalized;
        }
        return normalized.substring(0, STEM_PREVIEW_LIMIT) + "...";
    }

    private String normalizeQuestionType(String value, boolean allowNull) {
        String normalized = trimToNull(value);
        if (normalized != null) {
            normalized = normalized.toLowerCase(Locale.ROOT);
        }
        if (normalized == null && allowNull) {
            return null;
        }
        if (!QUESTION_TYPE_SINGLE.equals(normalized)
                && !QUESTION_TYPE_MULTIPLE.equals(normalized)
                && !QUESTION_TYPE_SHORT.equals(normalized)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题型仅支持 single、multiple、short");
        }
        return normalized;
    }

    private String normalizeRequiredText(String value, int maxLength, String emptyMessage, String lengthMessage) {
        String normalized = trimToNull(value);
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, emptyMessage);
        }
        if (normalized.length() > maxLength) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, lengthMessage);
        }
        return normalized;
    }

    private String normalizeNullableText(String value, int maxLength, String lengthMessage) {
        String normalized = trimToNull(value);
        if (!StringUtils.hasText(normalized)) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, lengthMessage);
        }
        return normalized;
    }

    private BigDecimal normalizeScore(BigDecimal score) {
        BigDecimal normalized = score == null ? new BigDecimal("1.00") : score.setScale(2, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0 || normalized.compareTo(new BigDecimal("9999.99")) > 0) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题目分值需保持在 0 到 9999.99 之间");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return StringUtils.hasText(trimmed) ? trimmed : null;
    }

    private void validateDifficultyLevel(Integer difficultyLevel, boolean allowNull) {
        if (difficultyLevel == null && allowNull) {
            return;
        }
        if (difficultyLevel == null || difficultyLevel < 1 || difficultyLevel > 3) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题目难度仅支持 1 到 3");
        }
    }

    private void validateYearNo(Integer yearNo, boolean allowNull) {
        if (yearNo == null && allowNull) {
            return;
        }
        int maxYear = LocalDate.now().getYear() + 1;
        if (yearNo == null || yearNo < 1900 || yearNo > maxYear) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "年份需保持在 1900 到 " + maxYear + " 之间");
        }
    }

    private void validateSortNo(Integer sortNo) {
        if (sortNo == null || sortNo < 0 || sortNo > 9999) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "排序值需保持在 0 到 9999 之间");
        }
    }

    private void validateStatus(Integer status, boolean allowNull) {
        if (status == null && allowNull) {
            return;
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题目状态仅支持 0 或 1");
        }
    }

    private void validateCategoryId(Long categoryId, Long existingCategoryId) {
        KyzzCategory category = requireCategory(categoryId);
        if (!Objects.equals(category.getIsEnabled(), 1) && !Objects.equals(existingCategoryId, categoryId)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "所选分类已停用，请改为启用中的分类");
        }
    }

    private KyzzQuestion requireQuestion(Long questionId) {
        if (questionId == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题目 ID 不能为空");
        }
        KyzzQuestion question = kyzzQuestionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "题目不存在");
        }
        return question;
    }

    private KyzzQuestionBank requireQuestionBank(Long questionBankId) {
        if (questionBankId == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "所属题库不能为空");
        }
        KyzzQuestionBank questionBank = kyzzQuestionBankMapper.selectById(questionBankId);
        if (questionBank == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "所选题库不存在");
        }
        return questionBank;
    }

    private KyzzCategory requireCategory(Long categoryId) {
        if (categoryId == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "分类 ID 不能为空");
        }
        KyzzCategory category = kyzzCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "所选分类不存在");
        }
        return category;
    }

    private record QueryFilter(long pageNo,
                               long pageSize,
                               String keyword,
                               Long questionBankId,
                               Long categoryId,
                               Long tagId,
                               String questionType,
                               Integer status,
                               Integer difficultyLevel,
                               Integer yearNo) {
    }

    private record NormalizedQuestionPayload(Long questionBankId,
                                             Long categoryId,
                                             String questionType,
                                             String stem,
                                             String analysis,
                                             String answerText,
                                             Integer difficultyLevel,
                                             BigDecimal score,
                                             String sourceName,
                                             Integer yearNo,
                                             Integer sortNo,
                                             Integer status,
                                             List<Long> tagIds,
                                             List<NormalizedOptionPayload> options) {
    }

    private record NormalizedOptionPayload(String optionKey,
                                           String optionContent,
                                           Integer isCorrect,
                                           Integer sortNo) {
    }
}
