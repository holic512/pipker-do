package org.example.backend.biz.kyzz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.backend.biz.kyzz.dto.KyzzPracticeBankRecordResponse;
import org.example.backend.biz.kyzz.dto.KyzzPracticeDashboardResponse;
import org.example.backend.biz.kyzz.dto.KyzzPracticeQuestionOptionResponse;
import org.example.backend.biz.kyzz.dto.KyzzPracticeQuestionResponse;
import org.example.backend.biz.kyzz.dto.KyzzPracticeReviewRequest;
import org.example.backend.biz.kyzz.dto.KyzzPracticeReviewResponse;
import org.example.backend.biz.kyzz.dto.KyzzPracticeSelfJudgementRequest;
import org.example.backend.biz.kyzz.dto.KyzzPracticeSessionProgressResponse;
import org.example.backend.biz.kyzz.dto.KyzzPracticeSessionResponse;
import org.example.backend.biz.kyzz.entity.KyzzCategory;
import org.example.backend.biz.kyzz.entity.KyzzQuestion;
import org.example.backend.biz.kyzz.entity.KyzzQuestionBank;
import org.example.backend.biz.kyzz.entity.KyzzQuestionOption;
import org.example.backend.biz.kyzz.entity.KyzzUserAnswer;
import org.example.backend.biz.kyzz.entity.KyzzUserQuestionBank;
import org.example.backend.biz.kyzz.entity.KyzzUserWrongQuestion;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionBankMapper;
import org.example.backend.biz.kyzz.mapper.KyzzUserAnswerMapper;
import org.example.backend.biz.kyzz.mapper.KyzzUserQuestionBankMapper;
import org.example.backend.biz.kyzz.mapper.KyzzUserWrongQuestionMapper;
import org.example.backend.biz.kyzz.support.KyzzCacheService;
import org.example.backend.biz.kyzz.support.KyzzPracticeSupport;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.security.anticrawler.AntiCrawlerSecurityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI 索引: KYZZ 用户侧刷题服务。
 */
@Service
public class KyzzPracticeUserService {

    private static final String QUESTION_TYPE_SINGLE = "single";
    private static final String QUESTION_TYPE_MULTIPLE = "multiple";
    private static final String QUESTION_TYPE_SHORT = "short";
    private static final Set<String> SUPPORTED_QUESTION_TYPES = Set.of(
            QUESTION_TYPE_SINGLE,
            QUESTION_TYPE_MULTIPLE,
            QUESTION_TYPE_SHORT
    );

    private final KyzzQuestionBankMapper kyzzQuestionBankMapper;
    private final KyzzUserQuestionBankMapper kyzzUserQuestionBankMapper;
    private final KyzzUserAnswerMapper kyzzUserAnswerMapper;
    private final KyzzUserWrongQuestionMapper kyzzUserWrongQuestionMapper;
    private final KyzzPracticeSupport kyzzPracticeSupport;
    private final AntiCrawlerSecurityService antiCrawlerSecurityService;
    private final KyzzCacheService kyzzCacheService;

    public KyzzPracticeUserService(KyzzQuestionBankMapper kyzzQuestionBankMapper,
                                   KyzzUserQuestionBankMapper kyzzUserQuestionBankMapper,
                                   KyzzUserAnswerMapper kyzzUserAnswerMapper,
                                   KyzzUserWrongQuestionMapper kyzzUserWrongQuestionMapper,
                                   KyzzPracticeSupport kyzzPracticeSupport,
                                   AntiCrawlerSecurityService antiCrawlerSecurityService,
                                   KyzzCacheService kyzzCacheService) {
        this.kyzzQuestionBankMapper = kyzzQuestionBankMapper;
        this.kyzzUserQuestionBankMapper = kyzzUserQuestionBankMapper;
        this.kyzzUserAnswerMapper = kyzzUserAnswerMapper;
        this.kyzzUserWrongQuestionMapper = kyzzUserWrongQuestionMapper;
        this.kyzzPracticeSupport = kyzzPracticeSupport;
        this.antiCrawlerSecurityService = antiCrawlerSecurityService;
        this.kyzzCacheService = kyzzCacheService;
    }

    public KyzzPracticeDashboardResponse getDashboard(Long userId) {
        return kyzzCacheService.getOrLoad(
                kyzzCacheService.userDashboardKey(userId),
                KyzzCacheService.USER_AGGREGATE_TTL,
                KyzzPracticeDashboardResponse.class,
                () -> loadDashboard(userId)
        );
    }

    private KyzzPracticeDashboardResponse loadDashboard(Long userId) {
        DashboardContext context = buildDashboardContext(userId);
        KyzzPracticeBankRecordResponse recommended = resolveRecommendedRecord(context.records());
        return new KyzzPracticeDashboardResponse(
                recommended == null ? null : recommended.getBankId(),
                buildRecommendedReason(recommended),
                context.records()
        );
    }

    public KyzzPracticeSessionResponse getSession(Long userId,
                                                  Long bankId,
                                                  Long questionId,
                                                  Boolean freshAttempt) {
        DashboardContext context = buildDashboardContext(userId);
        if (context.records().isEmpty()) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "还没有可刷题库，先去添加一套题库吧");
        }

        KyzzPracticeBankRecordResponse recommended = resolveRecommendedRecord(context.records());
        Long resolvedBankId = bankId;
        if (resolvedBankId == null) {
            if (recommended == null || recommended.getBankId() == null) {
                throw new BusinessException(ApiResponseCode.NOT_FOUND, "当前没有可续刷的题库");
            }
            resolvedBankId = recommended.getBankId();
        }

        KyzzPracticeBankRecordResponse activeBank = context.recordMap().get(resolvedBankId);
        if (activeBank == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "请先从已选择题库中开始刷题");
        }

        List<KyzzQuestion> questions = context.questionMap().getOrDefault(resolvedBankId, List.of());
        if (questions.isEmpty()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前题库暂无可练习题目");
        }

        KyzzQuestion targetQuestion = resolveTargetQuestion(resolvedBankId, questionId, questions, activeBank);
        Map<Long, List<KyzzQuestionOption>> optionMap = kyzzPracticeSupport.buildQuestionOptionMap(List.of(targetQuestion.getId()));
        Map<Long, KyzzUserAnswer> latestAnswerMap = kyzzPracticeSupport.buildLatestAnswerByBankQuestionMap(userId, List.of(resolvedBankId))
                .getOrDefault(resolvedBankId, Map.of());
        int currentQuestionIndex = findQuestionIndex(questions, targetQuestion.getId());
        KyzzQuestion previousQuestion = currentQuestionIndex > 1 ? questions.get(currentQuestionIndex - 2) : null;
        return new KyzzPracticeSessionResponse(
                activeBank,
                context.records(),
                new KyzzPracticeSessionProgressResponse(currentQuestionIndex, questions.size()),
                toQuestionResponse(targetQuestion, optionMap.getOrDefault(targetQuestion.getId(), List.of())),
                previousQuestion == null ? null : previousQuestion.getId(),
                previousQuestion == null ? null : currentQuestionIndex - 1,
                Boolean.TRUE.equals(freshAttempt)
                        ? null
                        : buildHistoricalReviewResult(
                                activeBank,
                                targetQuestion,
                                questions,
                                currentQuestionIndex,
                                optionMap.getOrDefault(targetQuestion.getId(), List.of()),
                                latestAnswerMap.get(targetQuestion.getId())
                        )
        );
    }

    @Transactional
    public KyzzPracticeReviewResponse reviewQuestion(Long userId,
                                                     Long questionId,
                                                     KyzzPracticeReviewRequest request) {
        PracticeQuestionContext context = requirePracticeQuestionContext(userId, questionId, request == null ? null : request.getBankId());
        validateUsedSeconds(request == null ? null : request.getUsedSeconds());
        String questionType = context.question().getQuestionType();

        if (QUESTION_TYPE_SHORT.equals(questionType)) {
            return buildShortQuestionPreviewResponse(context, request);
        }

        antiCrawlerSecurityService.inspectPracticeSubmitBehavior(userId, request == null ? null : request.getUsedSeconds());
        ObjectiveAnswerResult answerResult = gradeObjectiveQuestion(context, request);
        persistFinalAnswer(
                userId,
                context.bank().getId(),
                context.question().getId(),
                buildChoiceAnswerContent(answerResult.submittedOptionKeys()),
                request.getUsedSeconds(),
                answerResult.correct()
        );
        syncWrongQuestion(userId, context.bank().getId(), context.question().getId(), answerResult.correct());
        KyzzPracticeBankRecordResponse updatedBank = refreshBankRecord(userId, context.bank().getId());
        kyzzCacheService.evictUserAggregateCaches(userId);
        return new KyzzPracticeReviewResponse(
                context.question().getId(),
                context.bank().getId(),
                questionType,
                answerResult.submittedOptionKeys(),
                null,
                false,
                answerResult.correct(),
                answerResult.correctOptionKeys(),
                context.question().getAnswerText(),
                context.question().getAnalysis(),
                updatedBank,
                updatedBank.getResumeQuestionId(),
                updatedBank.getResumeQuestionIndex(),
                KyzzPracticeSupport.RESUME_STATUS_COMPLETED.equals(updatedBank.getResumeStatus())
        );
    }

    @Transactional
    public KyzzPracticeReviewResponse selfJudgeShortQuestion(Long userId,
                                                             Long questionId,
                                                             KyzzPracticeSelfJudgementRequest request) {
        PracticeQuestionContext context = requirePracticeQuestionContext(userId, questionId, request == null ? null : request.getBankId());
        if (!QUESTION_TYPE_SHORT.equals(context.question().getQuestionType())) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前题目不支持简答自判");
        }
        if (request == null || request.getSelfJudgedCorrect() == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "请先确认本题是否答对");
        }
        validateUsedSeconds(request.getUsedSeconds());

        antiCrawlerSecurityService.inspectPracticeSubmitBehavior(userId, request.getUsedSeconds());
        String submittedAnswerText = trimToEmpty(request.getAnswerText());
        persistFinalAnswer(
                userId,
                context.bank().getId(),
                context.question().getId(),
                submittedAnswerText,
                request.getUsedSeconds(),
                request.getSelfJudgedCorrect()
        );
        syncWrongQuestion(userId, context.bank().getId(), context.question().getId(), request.getSelfJudgedCorrect());
        KyzzPracticeBankRecordResponse updatedBank = refreshBankRecord(userId, context.bank().getId());
        kyzzCacheService.evictUserAggregateCaches(userId);
        return new KyzzPracticeReviewResponse(
                context.question().getId(),
                context.bank().getId(),
                context.question().getQuestionType(),
                List.of(),
                submittedAnswerText,
                false,
                request.getSelfJudgedCorrect(),
                List.of(),
                context.question().getAnswerText(),
                context.question().getAnalysis(),
                updatedBank,
                updatedBank.getResumeQuestionId(),
                updatedBank.getResumeQuestionIndex(),
                KyzzPracticeSupport.RESUME_STATUS_COMPLETED.equals(updatedBank.getResumeStatus())
        );
    }

    private DashboardContext buildDashboardContext(Long userId) {
        List<KyzzUserQuestionBank> relations = kyzzUserQuestionBankMapper.selectList(new LambdaQueryWrapper<KyzzUserQuestionBank>()
                .eq(KyzzUserQuestionBank::getUserId, userId)
                .orderByDesc(KyzzUserQuestionBank::getCreatedAt)
                .orderByDesc(KyzzUserQuestionBank::getId));
        if (relations.isEmpty()) {
            return DashboardContext.empty();
        }

        Map<Long, KyzzUserQuestionBank> relationMap = relations.stream()
                .collect(Collectors.toMap(
                        KyzzUserQuestionBank::getQuestionBankId,
                        item -> item,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        List<KyzzQuestionBank> activeBanks = kyzzQuestionBankMapper.selectList(new LambdaQueryWrapper<KyzzQuestionBank>()
                .in(KyzzQuestionBank::getId, relationMap.keySet())
                .eq(KyzzQuestionBank::getStatus, 1));
        if (activeBanks.isEmpty()) {
            return DashboardContext.empty();
        }

        Map<Long, List<KyzzQuestion>> questionMap = kyzzPracticeSupport.buildActiveQuestionMap(toBankIds(activeBanks));
        List<KyzzQuestionBank> practiceBanks = activeBanks.stream()
                .filter(bank -> !questionMap.getOrDefault(bank.getId(), List.of()).isEmpty())
                .toList();
        if (practiceBanks.isEmpty()) {
            return DashboardContext.empty();
        }

        Map<Long, KyzzCategory> categoryMap = kyzzPracticeSupport.buildCategoryMap();
        Map<Long, Map<Long, KyzzUserAnswer>> latestAnswerMap = kyzzPracticeSupport.buildLatestAnswerByBankQuestionMap(userId, toBankIds(practiceBanks));
        Map<Long, KyzzPracticeSupport.QuestionBankProgressSnapshot> progressMap = kyzzPracticeSupport.buildProgressSnapshotMap(
                toBankIds(practiceBanks),
                practiceBanks,
                latestAnswerMap
        );
        Map<Long, KyzzPracticeSupport.QuestionBankResumeSnapshot> resumeMap = kyzzPracticeSupport.buildResumeSnapshotMap(
                practiceBanks,
                questionMap,
                latestAnswerMap
        );

        List<KyzzPracticeBankRecordResponse> records = practiceBanks.stream()
                .map(bank -> toPracticeBankRecord(
                        bank,
                        relationMap.get(bank.getId()),
                        categoryMap.get(bank.getCategoryId()),
                        progressMap.get(bank.getId()),
                        resumeMap.get(bank.getId())
                ))
                .sorted(this::compareDashboardRecord)
                .toList();

        Map<Long, KyzzPracticeBankRecordResponse> recordMap = records.stream()
                .collect(Collectors.toMap(
                        KyzzPracticeBankRecordResponse::getBankId,
                        item -> item,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        return new DashboardContext(records, recordMap, questionMap);
    }

    private KyzzPracticeReviewResponse buildShortQuestionPreviewResponse(PracticeQuestionContext context,
                                                                         KyzzPracticeReviewRequest request) {
        KyzzPracticeBankRecordResponse currentBank = buildCurrentBankRecord(context);
        return new KyzzPracticeReviewResponse(
                context.question().getId(),
                context.bank().getId(),
                context.question().getQuestionType(),
                List.of(),
                trimToEmpty(request == null ? null : request.getAnswerText()),
                true,
                null,
                List.of(),
                context.question().getAnswerText(),
                context.question().getAnalysis(),
                currentBank,
                null,
                null,
                false
        );
    }

    private KyzzPracticeReviewResponse buildHistoricalReviewResult(KyzzPracticeBankRecordResponse activeBank,
                                                                   KyzzQuestion question,
                                                                   List<KyzzQuestion> questions,
                                                                   int currentQuestionIndex,
                                                                   List<KyzzQuestionOption> options,
                                                                   KyzzUserAnswer latestAnswer) {
        if (latestAnswer == null || !Objects.equals(latestAnswer.getAnswerStatus(), 1)) {
            return null;
        }
        List<String> correctOptionKeys = options.stream()
                .filter(option -> Objects.equals(option.getIsCorrect(), 1))
                .map(KyzzQuestionOption::getOptionKey)
                .filter(StringUtils::hasText)
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .sorted()
                .toList();
        Long nextQuestionId = null;
        Integer nextQuestionIndex = null;
        boolean completedBank = false;
        if (currentQuestionIndex < questions.size()) {
            KyzzQuestion nextQuestion = questions.get(currentQuestionIndex);
            nextQuestionId = nextQuestion.getId();
            nextQuestionIndex = currentQuestionIndex + 1;
        } else if (KyzzPracticeSupport.RESUME_STATUS_COMPLETED.equals(activeBank.getResumeStatus())
                && activeBank.getResumeQuestionId() != null) {
            nextQuestionId = activeBank.getResumeQuestionId();
            nextQuestionIndex = activeBank.getResumeQuestionIndex();
            completedBank = true;
        }

        if (QUESTION_TYPE_SHORT.equals(question.getQuestionType())) {
            return new KyzzPracticeReviewResponse(
                    question.getId(),
                    activeBank.getBankId(),
                    question.getQuestionType(),
                    List.of(),
                    trimToEmpty(latestAnswer.getAnswerContent()),
                    false,
                    Objects.equals(latestAnswer.getIsCorrect(), 1),
                    List.of(),
                    question.getAnswerText(),
                    question.getAnalysis(),
                    activeBank,
                    nextQuestionId,
                    nextQuestionIndex,
                    completedBank
            );
        }

        return new KyzzPracticeReviewResponse(
                question.getId(),
                activeBank.getBankId(),
                question.getQuestionType(),
                parseStoredOptionKeys(latestAnswer.getAnswerContent()),
                null,
                false,
                Objects.equals(latestAnswer.getIsCorrect(), 1),
                correctOptionKeys,
                question.getAnswerText(),
                question.getAnalysis(),
                activeBank,
                nextQuestionId,
                nextQuestionIndex,
                completedBank
        );
    }

    private ObjectiveAnswerResult gradeObjectiveQuestion(PracticeQuestionContext context,
                                                         KyzzPracticeReviewRequest request) {
        String questionType = context.question().getQuestionType();
        List<String> submittedOptionKeys = normalizeOptionKeys(request == null ? null : request.getSelectedOptionKeys());
        List<String> correctOptionKeys = context.optionMap().getOrDefault(context.question().getId(), List.of()).stream()
                .filter(option -> Objects.equals(option.getIsCorrect(), 1))
                .map(KyzzQuestionOption::getOptionKey)
                .filter(StringUtils::hasText)
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .sorted()
                .toList();

        if (QUESTION_TYPE_SINGLE.equals(questionType)) {
            if (submittedOptionKeys.size() != 1) {
                throw new BusinessException(ApiResponseCode.BAD_REQUEST, "单选题必须且只能选择 1 个选项");
            }
        } else if (QUESTION_TYPE_MULTIPLE.equals(questionType)) {
            if (submittedOptionKeys.isEmpty()) {
                throw new BusinessException(ApiResponseCode.BAD_REQUEST, "多选题至少选择 1 个选项后再查看答案");
            }
        } else {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前题型不支持选择题判分");
        }

        boolean correct = submittedOptionKeys.equals(correctOptionKeys);
        return new ObjectiveAnswerResult(submittedOptionKeys, correctOptionKeys, correct);
    }

    private PracticeQuestionContext requirePracticeQuestionContext(Long userId, Long questionId, Long bankId) {
        if (bankId == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题库不能为空");
        }
        KyzzQuestionBank bank = kyzzPracticeSupport.requireActiveQuestionBank(bankId);
        KyzzUserQuestionBank relation = kyzzUserQuestionBankMapper.selectOne(new LambdaQueryWrapper<KyzzUserQuestionBank>()
                .eq(KyzzUserQuestionBank::getUserId, userId)
                .eq(KyzzUserQuestionBank::getQuestionBankId, bankId)
                .last("limit 1"));
        if (relation == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "请先把题库加入我的题库再开始刷题");
        }
        List<KyzzQuestion> questions = kyzzPracticeSupport.buildActiveQuestionMap(List.of(bankId)).getOrDefault(bankId, List.of());
        if (questions.isEmpty()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前题库暂无可练习题目");
        }
        KyzzQuestion question = questions.stream()
                .filter(item -> Objects.equals(item.getId(), questionId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ApiResponseCode.NOT_FOUND, "题目不存在或已下架"));
        Map<Long, List<KyzzQuestionOption>> optionMap = kyzzPracticeSupport.buildQuestionOptionMap(List.of(questionId));
        return new PracticeQuestionContext(bank, relation, question, questions, optionMap);
    }

    private KyzzPracticeBankRecordResponse refreshBankRecord(Long userId, Long bankId) {
        KyzzQuestionBank bank = kyzzPracticeSupport.requireActiveQuestionBank(bankId);
        KyzzUserQuestionBank relation = kyzzUserQuestionBankMapper.selectOne(new LambdaQueryWrapper<KyzzUserQuestionBank>()
                .eq(KyzzUserQuestionBank::getUserId, userId)
                .eq(KyzzUserQuestionBank::getQuestionBankId, bankId)
                .last("limit 1"));
        if (relation == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题库关系不存在，无法更新刷题进度");
        }
        Map<Long, KyzzPracticeSupport.QuestionBankProgressSnapshot> progressMap = kyzzPracticeSupport.buildProgressSnapshotMap(
                userId,
                List.of(bankId),
                List.of(bank)
        );
        KyzzPracticeSupport.QuestionBankProgressSnapshot snapshot = progressMap.getOrDefault(
                bankId,
                KyzzPracticeSupport.QuestionBankProgressSnapshot.empty()
        );
        kyzzPracticeSupport.syncRelationSnapshot(relation.getId(), snapshot);
        KyzzUserQuestionBank refreshedRelation = kyzzUserQuestionBankMapper.selectById(relation.getId());
        Map<Long, KyzzPracticeSupport.QuestionBankResumeSnapshot> resumeMap = kyzzPracticeSupport.buildResumeSnapshotMap(userId, List.of(bank));
        KyzzCategory category = bank.getCategoryId() == null ? null : kyzzPracticeSupport.buildCategoryMap().get(bank.getCategoryId());
        return toPracticeBankRecord(
                bank,
                refreshedRelation,
                category,
                snapshot,
                resumeMap.get(bankId)
        );
    }

    private KyzzPracticeBankRecordResponse buildCurrentBankRecord(PracticeQuestionContext context) {
        Map<Long, KyzzPracticeSupport.QuestionBankProgressSnapshot> progressMap = kyzzPracticeSupport.buildProgressSnapshotMap(
                context.relation().getUserId(),
                List.of(context.bank().getId()),
                List.of(context.bank())
        );
        Map<Long, KyzzPracticeSupport.QuestionBankResumeSnapshot> resumeMap = kyzzPracticeSupport.buildResumeSnapshotMap(
                context.relation().getUserId(),
                List.of(context.bank())
        );
        KyzzCategory category = context.bank().getCategoryId() == null ? null : kyzzPracticeSupport.buildCategoryMap().get(context.bank().getCategoryId());
        return toPracticeBankRecord(
                context.bank(),
                context.relation(),
                category,
                progressMap.get(context.bank().getId()),
                resumeMap.get(context.bank().getId())
        );
    }

    private void persistFinalAnswer(Long userId,
                                    Long bankId,
                                    Long questionId,
                                    String answerContent,
                                    Integer usedSeconds,
                                    boolean correct) {
        KyzzUserAnswer answer = new KyzzUserAnswer();
        answer.setUserId(userId);
        answer.setQuestionBankId(bankId);
        answer.setQuestionId(questionId);
        answer.setAnswerContent(answerContent);
        answer.setIsCorrect(correct ? 1 : 0);
        answer.setAnswerStatus(1);
        answer.setUsedSeconds(usedSeconds == null ? 0 : usedSeconds);
        answer.setSubmittedAt(LocalDateTime.now());
        kyzzUserAnswerMapper.insert(answer);
    }

    private void syncWrongQuestion(Long userId, Long bankId, Long questionId, boolean correct) {
        KyzzUserWrongQuestion existing = kyzzUserWrongQuestionMapper.selectOne(new LambdaQueryWrapper<KyzzUserWrongQuestion>()
                .eq(KyzzUserWrongQuestion::getUserId, userId)
                .eq(KyzzUserWrongQuestion::getQuestionId, questionId)
                .last("limit 1"));
        LocalDateTime now = LocalDateTime.now();
        if (correct) {
            if (existing != null) {
                existing.setQuestionBankId(bankId);
                existing.setIsMastered(1);
                existing.setMasteredAt(now);
                kyzzUserWrongQuestionMapper.updateById(existing);
            }
            return;
        }

        if (existing == null) {
            KyzzUserWrongQuestion wrongQuestion = new KyzzUserWrongQuestion();
            wrongQuestion.setUserId(userId);
            wrongQuestion.setQuestionId(questionId);
            wrongQuestion.setQuestionBankId(bankId);
            wrongQuestion.setFirstWrongAt(now);
            wrongQuestion.setLastWrongAt(now);
            wrongQuestion.setWrongCount(1);
            wrongQuestion.setIsMastered(0);
            wrongQuestion.setMasteredAt(null);
            kyzzUserWrongQuestionMapper.insert(wrongQuestion);
            return;
        }

        existing.setQuestionBankId(bankId);
        existing.setLastWrongAt(now);
        existing.setWrongCount((existing.getWrongCount() == null ? 0 : existing.getWrongCount()) + 1);
        existing.setIsMastered(0);
        existing.setMasteredAt(null);
        kyzzUserWrongQuestionMapper.updateById(existing);
    }

    private KyzzPracticeBankRecordResponse toPracticeBankRecord(KyzzQuestionBank bank,
                                                                KyzzUserQuestionBank relation,
                                                                KyzzCategory category,
                                                                KyzzPracticeSupport.QuestionBankProgressSnapshot progressSnapshot,
                                                                KyzzPracticeSupport.QuestionBankResumeSnapshot resumeSnapshot) {
        KyzzPracticeSupport.QuestionBankProgressSnapshot resolvedProgress = progressSnapshot == null
                ? KyzzPracticeSupport.QuestionBankProgressSnapshot.empty()
                : progressSnapshot;
        KyzzPracticeSupport.QuestionBankResumeSnapshot resolvedResume = resumeSnapshot == null
                ? KyzzPracticeSupport.QuestionBankResumeSnapshot.empty()
                : resumeSnapshot;
        return new KyzzPracticeBankRecordResponse(
                bank.getId(),
                bank.getBankName(),
                kyzzPracticeSupport.resolveCoverUrl(bank.getCoverUrl()),
                category == null ? null : category.getCategoryName(),
                bank.getDifficultyLevel(),
                bank.getQuestionCount() == null ? 0 : bank.getQuestionCount(),
                resolvedProgress.currentProgress(),
                resolvedProgress.studiedCount(),
                resolvedProgress.wrongCount(),
                resolvedProgress.lastPracticeAt(),
                resolvedResume.resumeStatus(),
                resolvedResume.resumeLabel(),
                resolvedResume.resumeQuestionId(),
                resolvedResume.resumeQuestionIndex(),
                relation == null ? null : relation.getCreatedAt()
        );
    }

    private KyzzPracticeQuestionResponse toQuestionResponse(KyzzQuestion question,
                                                            List<KyzzQuestionOption> options) {
        List<KyzzPracticeQuestionOptionResponse> optionResponses = options.stream()
                .map(option -> new KyzzPracticeQuestionOptionResponse(
                        option.getOptionKey(),
                        option.getOptionContent()
                ))
                .toList();
        return new KyzzPracticeQuestionResponse(
                question.getId(),
                question.getQuestionType(),
                question.getStem(),
                question.getDifficultyLevel(),
                question.getScore(),
                question.getSortNo(),
                question.getSourceName(),
                question.getYearNo(),
                optionResponses
        );
    }

    private KyzzPracticeBankRecordResponse resolveRecommendedRecord(List<KyzzPracticeBankRecordResponse> records) {
        if (records == null || records.isEmpty()) {
            return null;
        }

        List<KyzzPracticeBankRecordResponse> inProgress = records.stream()
                .filter(item -> KyzzPracticeSupport.RESUME_STATUS_IN_PROGRESS.equals(item.getResumeStatus()))
                .sorted(Comparator
                        .comparing(KyzzPracticeBankRecordResponse::getLastPracticeAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(KyzzPracticeBankRecordResponse::getJoinedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(KyzzPracticeBankRecordResponse::getBankId, Comparator.reverseOrder()))
                .toList();
        if (!inProgress.isEmpty()) {
            return inProgress.get(0);
        }

        List<KyzzPracticeBankRecordResponse> notStarted = records.stream()
                .filter(item -> KyzzPracticeSupport.RESUME_STATUS_NOT_STARTED.equals(item.getResumeStatus()))
                .sorted(Comparator
                        .comparing(KyzzPracticeBankRecordResponse::getJoinedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(KyzzPracticeBankRecordResponse::getBankId, Comparator.reverseOrder()))
                .toList();
        if (!notStarted.isEmpty()) {
            return notStarted.get(0);
        }

        return records.stream()
                .filter(item -> KyzzPracticeSupport.RESUME_STATUS_COMPLETED.equals(item.getResumeStatus()))
                .max(Comparator
                        .comparing(KyzzPracticeBankRecordResponse::getLastPracticeAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(KyzzPracticeBankRecordResponse::getJoinedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(KyzzPracticeBankRecordResponse::getBankId))
                .orElse(null);
    }

    private String buildRecommendedReason(KyzzPracticeBankRecordResponse record) {
        if (record == null) {
            return "先去添加一套题库，再开始刷题";
        }
        if (KyzzPracticeSupport.RESUME_STATUS_IN_PROGRESS.equals(record.getResumeStatus())) {
            return "继续上次未完成的进度";
        }
        if (KyzzPracticeSupport.RESUME_STATUS_NOT_STARTED.equals(record.getResumeStatus())) {
            return "从已加入的题库开始第一轮练习";
        }
        return "这套题库刚好适合重新回顾一遍";
    }

    private KyzzQuestion resolveTargetQuestion(Long bankId,
                                               Long questionId,
                                               List<KyzzQuestion> questions,
                                               KyzzPracticeBankRecordResponse activeBank) {
        if (questionId != null) {
            return questions.stream()
                    .filter(item -> Objects.equals(item.getId(), questionId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ApiResponseCode.NOT_FOUND, "题目不存在或已下架"));
        }
        if (activeBank.getResumeQuestionId() != null) {
            return questions.stream()
                    .filter(item -> Objects.equals(item.getId(), activeBank.getResumeQuestionId()))
                    .findFirst()
                    .orElse(questions.get(0));
        }
        if (questions.isEmpty()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前题库暂无可练习题目");
        }
        return questions.get(0);
    }

    private int compareDashboardRecord(KyzzPracticeBankRecordResponse left,
                                       KyzzPracticeBankRecordResponse right) {
        int statusCompare = Integer.compare(statusRank(left.getResumeStatus()), statusRank(right.getResumeStatus()));
        if (statusCompare != 0) {
            return statusCompare;
        }
        if (KyzzPracticeSupport.RESUME_STATUS_NOT_STARTED.equals(left.getResumeStatus())) {
            return compareDateDesc(left.getJoinedAt(), right.getJoinedAt(), left.getBankId(), right.getBankId());
        }
        return compareDateDesc(left.getLastPracticeAt(), right.getLastPracticeAt(), left.getBankId(), right.getBankId());
    }

    private int statusRank(String resumeStatus) {
        if (KyzzPracticeSupport.RESUME_STATUS_IN_PROGRESS.equals(resumeStatus)) {
            return 0;
        }
        if (KyzzPracticeSupport.RESUME_STATUS_NOT_STARTED.equals(resumeStatus)) {
            return 1;
        }
        return 2;
    }

    private int compareDateDesc(LocalDateTime left,
                                LocalDateTime right,
                                Long leftId,
                                Long rightId) {
        if (left == null && right == null) {
            return Comparator.<Long>nullsLast(Comparator.reverseOrder()).compare(leftId, rightId);
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        int compare = right.compareTo(left);
        if (compare != 0) {
            return compare;
        }
        return Comparator.<Long>nullsLast(Comparator.reverseOrder()).compare(leftId, rightId);
    }

    private int findQuestionIndex(List<KyzzQuestion> questions, Long questionId) {
        for (int index = 0; index < questions.size(); index++) {
            if (Objects.equals(questions.get(index).getId(), questionId)) {
                return index + 1;
            }
        }
        return 1;
    }

    private void validateUsedSeconds(Integer usedSeconds) {
        if (usedSeconds == null || usedSeconds < 0) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "答题耗时不能为空");
        }
    }

    private String buildChoiceAnswerContent(List<String> submittedOptionKeys) {
        if (submittedOptionKeys == null || submittedOptionKeys.isEmpty()) {
            return "[]";
        }
        return submittedOptionKeys.stream()
                .collect(Collectors.joining("\",\"", "[\"", "\"]"));
    }

    private List<String> parseStoredOptionKeys(String answerContent) {
        if (!StringUtils.hasText(answerContent)) {
            return List.of();
        }
        String normalized = answerContent.trim();
        if (normalized.startsWith("[") && normalized.endsWith("]")) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        if (!StringUtils.hasText(normalized)) {
            return List.of();
        }
        String[] segments = normalized.split(",");
        List<String> optionKeys = new ArrayList<>(segments.length);
        for (String segment : segments) {
            String value = trimToEmpty(segment)
                    .replace("\"", "")
                    .replace("'", "");
            if (StringUtils.hasText(value)) {
                optionKeys.add(value);
            }
        }
        return normalizeOptionKeys(optionKeys);
    }

    private List<String> normalizeOptionKeys(Collection<String> optionKeys) {
        if (optionKeys == null || optionKeys.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        optionKeys.stream()
                .filter(StringUtils::hasText)
                .map(item -> item.trim().toUpperCase(Locale.ROOT))
                .forEach(normalized::add);
        return normalized.stream().sorted().toList();
    }

    private List<Long> toBankIds(Collection<KyzzQuestionBank> banks) {
        if (banks == null || banks.isEmpty()) {
            return List.of();
        }
        List<Long> result = new ArrayList<>(banks.size());
        banks.forEach(bank -> result.add(bank.getId()));
        return result;
    }

    private String trimToEmpty(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private record DashboardContext(List<KyzzPracticeBankRecordResponse> records,
                                    Map<Long, KyzzPracticeBankRecordResponse> recordMap,
                                    Map<Long, List<KyzzQuestion>> questionMap) {

        private static DashboardContext empty() {
            return new DashboardContext(List.of(), Map.of(), Map.of());
        }
    }

    private record PracticeQuestionContext(KyzzQuestionBank bank,
                                           KyzzUserQuestionBank relation,
                                           KyzzQuestion question,
                                           List<KyzzQuestion> questions,
                                           Map<Long, List<KyzzQuestionOption>> optionMap) {
    }

    private record ObjectiveAnswerResult(List<String> submittedOptionKeys,
                                         List<String> correctOptionKeys,
                                         boolean correct) {
    }
}
