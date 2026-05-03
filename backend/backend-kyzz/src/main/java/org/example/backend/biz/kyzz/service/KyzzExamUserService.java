package org.example.backend.biz.kyzz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.biz.kyzz.dto.KyzzExamAnswerSaveRequest;
import org.example.backend.biz.kyzz.dto.KyzzExamAnswerSaveResponse;
import org.example.backend.biz.kyzz.dto.KyzzExamDetailResponse;
import org.example.backend.biz.kyzz.dto.KyzzExamDifficultyOptionResponse;
import org.example.backend.biz.kyzz.dto.KyzzExamEntryResponse;
import org.example.backend.biz.kyzz.dto.KyzzExamPresetResponse;
import org.example.backend.biz.kyzz.dto.KyzzExamQuestionOptionResponse;
import org.example.backend.biz.kyzz.dto.KyzzExamQuestionResponse;
import org.example.backend.biz.kyzz.dto.KyzzExamStartRequest;
import org.example.backend.biz.kyzz.dto.KyzzExamSummaryResponse;
import org.example.backend.biz.kyzz.entity.KyzzExamQuestion;
import org.example.backend.biz.kyzz.entity.KyzzExamSession;
import org.example.backend.biz.kyzz.entity.KyzzQuestion;
import org.example.backend.biz.kyzz.entity.KyzzQuestionBank;
import org.example.backend.biz.kyzz.entity.KyzzQuestionOption;
import org.example.backend.biz.kyzz.mapper.KyzzExamQuestionMapper;
import org.example.backend.biz.kyzz.mapper.KyzzExamSessionMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionBankMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionOptionMapper;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.account.dto.VipInfoResponse;
import org.example.backend.shared.account.service.UserProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * AI 索引: KYZZ VIP 考试组卷、会话和历史服务。
 */
@Service
public class KyzzExamUserService {

    private static final String QUESTION_TYPE_SINGLE = "single";
    private static final String QUESTION_TYPE_MULTIPLE = "multiple";
    private static final String QUESTION_TYPE_SHORT = "short";
    private static final String EXAM_TYPE_CHOICE = "choice";
    private static final String EXAM_TYPE_SHORT = "short";
    private static final String EXAM_TYPE_FULL = "full";
    private static final String DIFFICULTY_BALANCED = "balanced";
    private static final String DIFFICULTY_EASY = "easy";
    private static final String DIFFICULTY_NORMAL = "normal";
    private static final String DIFFICULTY_HARD = "hard";
    private static final String STATUS_IN_PROGRESS = "in_progress";
    private static final String STATUS_SUBMITTED = "submitted";
    private static final String STATUS_EXPIRED = "expired";
    private static final String GRADING_STATUS_NOT_STARTED = "not_started";
    private static final String GRADING_STATUS_GRADING = "grading";
    private static final String GRADING_STATUS_GRADED = "graded";
    private static final String GRADING_STATUS_FAILED = "failed";
    private static final int ANSWER_STATUS_BLANK = 0;
    private static final int ANSWER_STATUS_ANSWERED = 1;
    private static final int MAX_DURATION_MINUTES = 240;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Set<String> SUPPORTED_EXAM_TYPES = Set.of(EXAM_TYPE_CHOICE, EXAM_TYPE_SHORT, EXAM_TYPE_FULL);
    private static final Set<String> SUPPORTED_DIFFICULTY_MODES = Set.of(
            DIFFICULTY_BALANCED,
            DIFFICULTY_EASY,
            DIFFICULTY_NORMAL,
            DIFFICULTY_HARD
    );

    private final KyzzExamSessionMapper kyzzExamSessionMapper;
    private final KyzzExamQuestionMapper kyzzExamQuestionMapper;
    private final KyzzQuestionMapper kyzzQuestionMapper;
    private final KyzzQuestionOptionMapper kyzzQuestionOptionMapper;
    private final KyzzQuestionBankMapper kyzzQuestionBankMapper;
    private final UserProfileService userProfileService;
    private final KyzzExamGradingService kyzzExamGradingService;
    private final ObjectMapper objectMapper;

    public KyzzExamUserService(KyzzExamSessionMapper kyzzExamSessionMapper,
                               KyzzExamQuestionMapper kyzzExamQuestionMapper,
                               KyzzQuestionMapper kyzzQuestionMapper,
                               KyzzQuestionOptionMapper kyzzQuestionOptionMapper,
                               KyzzQuestionBankMapper kyzzQuestionBankMapper,
                               UserProfileService userProfileService,
                               KyzzExamGradingService kyzzExamGradingService,
                               ObjectMapper objectMapper) {
        this.kyzzExamSessionMapper = kyzzExamSessionMapper;
        this.kyzzExamQuestionMapper = kyzzExamQuestionMapper;
        this.kyzzQuestionMapper = kyzzQuestionMapper;
        this.kyzzQuestionOptionMapper = kyzzQuestionOptionMapper;
        this.kyzzQuestionBankMapper = kyzzQuestionBankMapper;
        this.userProfileService = userProfileService;
        this.kyzzExamGradingService = kyzzExamGradingService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public KyzzExamEntryResponse getEntry(Long userId) {
        VipInfoResponse vipInfo = userProfileService.getVipInfo(userId);
        expireOverdueExams(userId, LocalDateTime.now());
        KyzzExamSummaryResponse ongoingExam = vipInfo.isVip() ? toSummary(findOngoingExam(userId), LocalDateTime.now()) : null;
        return new KyzzExamEntryResponse(
                vipInfo,
                ongoingExam,
                buildPresets(),
                buildDifficultyOptions()
        );
    }

    public List<KyzzExamSummaryResponse> listHistory(Long userId, Integer limit) {
        requireVip(userId);
        LocalDateTime now = LocalDateTime.now();
        expireOverdueExams(userId, now);
        int safeLimit = limit == null ? 20 : Math.min(Math.max(limit, 1), 50);
        return kyzzExamSessionMapper.selectList(new LambdaQueryWrapper<KyzzExamSession>()
                        .eq(KyzzExamSession::getUserId, userId)
                        .orderByDesc(KyzzExamSession::getStartedAt)
                        .orderByDesc(KyzzExamSession::getId)
                        .last("limit " + safeLimit))
                .stream()
                .map(session -> toSummary(session, now))
                .toList();
    }

    @Transactional
    public KyzzExamDetailResponse startExam(Long userId, KyzzExamStartRequest request) {
        requireVip(userId);
        LocalDateTime now = LocalDateTime.now();
        expireOverdueExams(userId, now);
        KyzzExamSession ongoingExam = findOngoingExam(userId);
        if (ongoingExam != null) {
            throw new BusinessException(ApiResponseCode.CONFLICT, "你还有未完成的考试，先继续或交卷后再开始新的考试");
        }

        String examType = normalizeExamType(request == null ? null : request.getExamType());
        String difficultyMode = normalizeDifficultyMode(request == null ? null : request.getDifficultyMode());
        ExamComposition composition = resolveComposition(examType);
        int durationMinutes = normalizeDurationMinutes(
                request == null ? null : request.getDurationMinutes(),
                composition.defaultDurationMinutes()
        );
        List<KyzzQuestion> selectedQuestions = selectExamQuestions(composition, difficultyMode);
        if (selectedQuestions.isEmpty()) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "当前题库暂无可用于考试的题目");
        }

        KyzzExamSession session = new KyzzExamSession();
        session.setUserId(userId);
        session.setExamNo(generateExamNo());
        session.setExamType(examType);
        session.setDifficultyMode(difficultyMode);
        session.setDurationMinutes(durationMinutes);
        session.setSingleCount(countByType(selectedQuestions, QUESTION_TYPE_SINGLE));
        session.setMultipleCount(countByType(selectedQuestions, QUESTION_TYPE_MULTIPLE));
        session.setShortCount(countByType(selectedQuestions, QUESTION_TYPE_SHORT));
        session.setTotalQuestionCount(selectedQuestions.size());
        session.setAnsweredCount(0);
        session.setTotalScore(calculateTotalScore(selectedQuestions));
        session.setStatus(STATUS_IN_PROGRESS);
        session.setStartedAt(now);
        session.setDeadlineAt(now.plusMinutes(durationMinutes));
        kyzzExamSessionMapper.insert(session);

        int order = 1;
        for (KyzzQuestion question : selectedQuestions) {
            KyzzExamQuestion examQuestion = new KyzzExamQuestion();
            examQuestion.setSessionId(session.getId());
            examQuestion.setQuestionId(question.getId());
            examQuestion.setQuestionBankId(question.getQuestionBankId());
            examQuestion.setQuestionType(question.getQuestionType());
            examQuestion.setQuestionOrder(order++);
            examQuestion.setScore(resolveExamScore(question));
            examQuestion.setAnswerStatus(ANSWER_STATUS_BLANK);
            examQuestion.setUsedSeconds(0);
            kyzzExamQuestionMapper.insert(examQuestion);
        }
        return getExamDetail(userId, session.getId());
    }

    @Transactional
    public KyzzExamDetailResponse getExamDetail(Long userId, Long sessionId) {
        requireVip(userId);
        LocalDateTime now = LocalDateTime.now();
        KyzzExamSession session = requireOwnedSession(userId, sessionId);
        if (STATUS_IN_PROGRESS.equals(session.getStatus()) && isDeadlinePassed(session, now)) {
            markExpired(session.getId());
            session = requireOwnedSession(userId, sessionId);
        }
        return toDetail(session, now);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public KyzzExamAnswerSaveResponse saveAnswer(Long userId,
                                                 Long sessionId,
                                                 Long questionId,
                                                 KyzzExamAnswerSaveRequest request) {
        requireVip(userId);
        KyzzExamSession session = requireOwnedSession(userId, sessionId);
        ensureCanAnswer(session);
        KyzzExamQuestion examQuestion = requireExamQuestion(sessionId, questionId);

        String answerContent = normalizeAnswerContent(examQuestion.getQuestionType(), request);
        int answerStatus = StringUtils.hasText(answerContent) ? ANSWER_STATUS_ANSWERED : ANSWER_STATUS_BLANK;
        int usedSeconds = normalizeUsedSeconds(request == null ? null : request.getUsedSeconds());
        LocalDateTime now = LocalDateTime.now();
        kyzzExamQuestionMapper.update(null, new LambdaUpdateWrapper<KyzzExamQuestion>()
                .eq(KyzzExamQuestion::getId, examQuestion.getId())
                .set(KyzzExamQuestion::getAnswerContent, answerContent)
                .set(KyzzExamQuestion::getAnswerStatus, answerStatus)
                .set(KyzzExamQuestion::getUsedSeconds, usedSeconds)
                .set(KyzzExamQuestion::getAnsweredAt, answerStatus == ANSWER_STATUS_ANSWERED ? now : null));
        int answeredCount = refreshAnsweredCount(sessionId);
        return new KyzzExamAnswerSaveResponse(
                sessionId,
                questionId,
                answeredCount,
                session.getTotalQuestionCount()
        );
    }

    @Transactional
    public KyzzExamSummaryResponse submitExam(Long userId, Long sessionId) {
        requireVip(userId);
        KyzzExamSession session = requireOwnedSession(userId, sessionId);
        if (!STATUS_IN_PROGRESS.equals(session.getStatus())) {
            return toSummary(session, LocalDateTime.now());
        }
        int answeredCount = refreshAnsweredCount(sessionId);
        LocalDateTime now = LocalDateTime.now();
        kyzzExamSessionMapper.update(null, new LambdaUpdateWrapper<KyzzExamSession>()
                .eq(KyzzExamSession::getId, sessionId)
                .eq(KyzzExamSession::getStatus, STATUS_IN_PROGRESS)
                .set(KyzzExamSession::getAnsweredCount, answeredCount)
                .set(KyzzExamSession::getEarnedScore, BigDecimal.ZERO)
                .set(KyzzExamSession::getObjectiveScore, BigDecimal.ZERO)
                .set(KyzzExamSession::getSubjectiveScore, BigDecimal.ZERO)
                .set(KyzzExamSession::getStatus, STATUS_SUBMITTED)
                .set(KyzzExamSession::getGradingStatus, GRADING_STATUS_GRADING)
                .set(KyzzExamSession::getSubmittedAt, now)
                .set(KyzzExamSession::getGradingStartedAt, null)
                .set(KyzzExamSession::getGradedAt, null)
                .set(KyzzExamSession::getGradingErrorMessage, null));
        enqueueGradingAfterCommit(sessionId);
        return toSummary(requireOwnedSession(userId, sessionId), now);
    }

    @Transactional
    public KyzzExamSummaryResponse retryGrading(Long userId, Long sessionId) {
        requireVip(userId);
        KyzzExamSession session = requireOwnedSession(userId, sessionId);
        if (STATUS_IN_PROGRESS.equals(session.getStatus())) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "考试进行中，不能重新阅卷");
        }
        if (!GRADING_STATUS_FAILED.equals(session.getGradingStatus())) {
            throw new BusinessException(ApiResponseCode.CONFLICT, "当前考试不需要重新阅卷");
        }

        kyzzExamQuestionMapper.update(null, new LambdaUpdateWrapper<KyzzExamQuestion>()
                .eq(KyzzExamQuestion::getSessionId, sessionId)
                .set(KyzzExamQuestion::getAwardedScore, BigDecimal.ZERO)
                .set(KyzzExamQuestion::getIsCorrect, null)
                .set(KyzzExamQuestion::getGradingStatus, GRADING_STATUS_NOT_STARTED)
                .set(KyzzExamQuestion::getGradingMethod, null)
                .set(KyzzExamQuestion::getReferenceAnswer, null)
                .set(KyzzExamQuestion::getAnalysisSnapshot, null)
                .set(KyzzExamQuestion::getGradingComment, null)
                .set(KyzzExamQuestion::getGradingConfidence, null)
                .set(KyzzExamQuestion::getGradingPointsJson, null)
                .set(KyzzExamQuestion::getLlmRecordId, null)
                .set(KyzzExamQuestion::getGradedAt, null));
        kyzzExamSessionMapper.update(null, new LambdaUpdateWrapper<KyzzExamSession>()
                .eq(KyzzExamSession::getId, sessionId)
                .eq(KyzzExamSession::getGradingStatus, GRADING_STATUS_FAILED)
                .set(KyzzExamSession::getEarnedScore, BigDecimal.ZERO)
                .set(KyzzExamSession::getObjectiveScore, BigDecimal.ZERO)
                .set(KyzzExamSession::getSubjectiveScore, BigDecimal.ZERO)
                .set(KyzzExamSession::getGradingStatus, GRADING_STATUS_GRADING)
                .set(KyzzExamSession::getGradingStartedAt, null)
                .set(KyzzExamSession::getGradedAt, null)
                .set(KyzzExamSession::getGradingErrorMessage, null));
        enqueueGradingAfterCommit(sessionId);
        return toSummary(requireOwnedSession(userId, sessionId), LocalDateTime.now());
    }

    private void requireVip(Long userId) {
        VipInfoResponse vipInfo = userProfileService.getVipInfo(userId);
        if (!vipInfo.isVip()) {
            throw new BusinessException(ApiResponseCode.FORBIDDEN, "当前功能仅 VIP 可用");
        }
    }

    private KyzzExamSession findOngoingExam(Long userId) {
        return kyzzExamSessionMapper.selectOne(new LambdaQueryWrapper<KyzzExamSession>()
                .eq(KyzzExamSession::getUserId, userId)
                .eq(KyzzExamSession::getStatus, STATUS_IN_PROGRESS)
                .orderByDesc(KyzzExamSession::getStartedAt)
                .orderByDesc(KyzzExamSession::getId)
                .last("limit 1"));
    }

    private KyzzExamSession requireOwnedSession(Long userId, Long sessionId) {
        if (sessionId == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "考试ID不能为空");
        }
        KyzzExamSession session = kyzzExamSessionMapper.selectById(sessionId);
        if (session == null || !Objects.equals(session.getUserId(), userId)) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "考试记录不存在");
        }
        return session;
    }

    private KyzzExamQuestion requireExamQuestion(Long sessionId, Long questionId) {
        if (questionId == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题目ID不能为空");
        }
        KyzzExamQuestion examQuestion = kyzzExamQuestionMapper.selectOne(new LambdaQueryWrapper<KyzzExamQuestion>()
                .eq(KyzzExamQuestion::getSessionId, sessionId)
                .eq(KyzzExamQuestion::getQuestionId, questionId)
                .last("limit 1"));
        if (examQuestion == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "考试题目不存在");
        }
        return examQuestion;
    }

    private void ensureCanAnswer(KyzzExamSession session) {
        if (!STATUS_IN_PROGRESS.equals(session.getStatus())) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前考试已结束，不能继续作答");
        }
        if (isDeadlinePassed(session, LocalDateTime.now())) {
            markExpired(session.getId());
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "考试时间已结束");
        }
    }

    private void expireOverdueExams(Long userId, LocalDateTime now) {
        List<KyzzExamSession> overdueSessions = kyzzExamSessionMapper.selectList(new LambdaQueryWrapper<KyzzExamSession>()
                .eq(KyzzExamSession::getUserId, userId)
                .eq(KyzzExamSession::getStatus, STATUS_IN_PROGRESS)
                .lt(KyzzExamSession::getDeadlineAt, now));
        overdueSessions.forEach(session -> markExpired(session.getId()));
    }

    private void markExpired(Long sessionId) {
        int answeredCount = refreshAnsweredCount(sessionId);
        int updated = kyzzExamSessionMapper.update(null, new LambdaUpdateWrapper<KyzzExamSession>()
                .eq(KyzzExamSession::getId, sessionId)
                .eq(KyzzExamSession::getStatus, STATUS_IN_PROGRESS)
                .set(KyzzExamSession::getAnsweredCount, answeredCount)
                .set(KyzzExamSession::getEarnedScore, BigDecimal.ZERO)
                .set(KyzzExamSession::getObjectiveScore, BigDecimal.ZERO)
                .set(KyzzExamSession::getSubjectiveScore, BigDecimal.ZERO)
                .set(KyzzExamSession::getStatus, STATUS_EXPIRED)
                .set(KyzzExamSession::getGradingStatus, GRADING_STATUS_GRADING)
                .set(KyzzExamSession::getGradingStartedAt, null)
                .set(KyzzExamSession::getGradedAt, null)
                .set(KyzzExamSession::getGradingErrorMessage, null));
        if (updated > 0) {
            enqueueGradingAfterCommit(sessionId);
        }
    }

    private boolean isDeadlinePassed(KyzzExamSession session, LocalDateTime now) {
        return session.getDeadlineAt() != null && now.isAfter(session.getDeadlineAt());
    }

    private int refreshAnsweredCount(Long sessionId) {
        int answeredCount = Math.toIntExact(kyzzExamQuestionMapper.selectCount(new LambdaQueryWrapper<KyzzExamQuestion>()
                .eq(KyzzExamQuestion::getSessionId, sessionId)
                .eq(KyzzExamQuestion::getAnswerStatus, ANSWER_STATUS_ANSWERED)));
        kyzzExamSessionMapper.update(null, new LambdaUpdateWrapper<KyzzExamSession>()
                .eq(KyzzExamSession::getId, sessionId)
                .set(KyzzExamSession::getAnsweredCount, answeredCount));
        return answeredCount;
    }

    private KyzzExamDetailResponse toDetail(KyzzExamSession session, LocalDateTime now) {
        List<KyzzExamQuestion> examQuestions = kyzzExamQuestionMapper.selectList(new LambdaQueryWrapper<KyzzExamQuestion>()
                .eq(KyzzExamQuestion::getSessionId, session.getId())
                .orderByAsc(KyzzExamQuestion::getQuestionOrder)
                .orderByAsc(KyzzExamQuestion::getId));
        Map<Long, KyzzQuestion> questionMap = loadQuestionMap(examQuestions.stream().map(KyzzExamQuestion::getQuestionId).toList());
        Map<Long, List<KyzzQuestionOption>> optionMap = loadOptionMap(questionMap.keySet());
        boolean includeGrading = !STATUS_IN_PROGRESS.equals(session.getStatus());
        List<KyzzExamQuestionResponse> questions = examQuestions.stream()
                .map(examQuestion -> toQuestionResponse(
                        examQuestion,
                        questionMap.get(examQuestion.getQuestionId()),
                        optionMap.getOrDefault(examQuestion.getQuestionId(), List.of()),
                        includeGrading
                ))
                .toList();
        boolean canAnswer = STATUS_IN_PROGRESS.equals(session.getStatus()) && !isDeadlinePassed(session, now);
        return new KyzzExamDetailResponse(
                toSummary(session, now),
                questions,
                canAnswer,
                canAnswer
        );
    }

    private KyzzExamQuestionResponse toQuestionResponse(KyzzExamQuestion examQuestion,
                                                       KyzzQuestion question,
                                                       List<KyzzQuestionOption> options,
                                                       boolean includeGrading) {
        if (question == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "考试题目已失效");
        }
        List<String> correctOptionKeys = includeGrading && isChoiceQuestion(examQuestion.getQuestionType())
                ? resolveCorrectOptionKeys(options)
                : List.of();
        GradingPoints gradingPoints = includeGrading
                ? parseGradingPoints(examQuestion.getGradingPointsJson())
                : GradingPoints.empty();
        return new KyzzExamQuestionResponse(
                examQuestion.getId(),
                question.getId(),
                question.getQuestionBankId(),
                examQuestion.getQuestionType(),
                examQuestion.getQuestionOrder(),
                question.getStem(),
                question.getDifficultyLevel(),
                examQuestion.getScore(),
                includeGrading ? examQuestion.getAwardedScore() : null,
                question.getSourceName(),
                question.getYearNo(),
                options.stream()
                        .sorted(Comparator.comparing(KyzzQuestionOption::getSortNo, Comparator.nullsLast(Integer::compareTo))
                                .thenComparing(KyzzQuestionOption::getId))
                        .map(option -> new KyzzExamQuestionOptionResponse(option.getOptionKey(), option.getOptionContent()))
                        .toList(),
                isChoiceQuestion(examQuestion.getQuestionType()) ? parseChoiceAnswerContent(examQuestion.getAnswerContent()) : List.of(),
                correctOptionKeys,
                QUESTION_TYPE_SHORT.equals(examQuestion.getQuestionType()) ? examQuestion.getAnswerContent() : null,
                examQuestion.getAnswerStatus(),
                includeGrading ? examQuestion.getIsCorrect() : null,
                includeGrading ? examQuestion.getGradingStatus() : null,
                includeGrading ? examQuestion.getGradingMethod() : null,
                includeGrading ? firstText(examQuestion.getReferenceAnswer(), question.getAnswerText()) : null,
                includeGrading ? firstText(examQuestion.getAnalysisSnapshot(), question.getAnalysis()) : null,
                includeGrading ? examQuestion.getGradingComment() : null,
                includeGrading ? examQuestion.getGradingConfidence() : null,
                gradingPoints.matchedPoints(),
                gradingPoints.missingPoints(),
                includeGrading ? examQuestion.getLlmRecordId() : null,
                includeGrading ? formatTime(examQuestion.getGradedAt()) : null
        );
    }

    private Map<Long, KyzzQuestion> loadQuestionMap(Collection<Long> questionIds) {
        LinkedHashSet<Long> ids = normalizeIds(questionIds);
        if (ids.isEmpty()) {
            return Map.of();
        }
        return kyzzQuestionMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(KyzzQuestion::getId, question -> question));
    }

    private Map<Long, List<KyzzQuestionOption>> loadOptionMap(Collection<Long> questionIds) {
        LinkedHashSet<Long> ids = normalizeIds(questionIds);
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<KyzzQuestionOption>> result = new LinkedHashMap<>();
        kyzzQuestionOptionMapper.selectList(new LambdaQueryWrapper<KyzzQuestionOption>()
                        .in(KyzzQuestionOption::getQuestionId, ids)
                        .orderByAsc(KyzzQuestionOption::getQuestionId)
                        .orderByAsc(KyzzQuestionOption::getSortNo)
                        .orderByAsc(KyzzQuestionOption::getId))
                .forEach(option -> result.computeIfAbsent(option.getQuestionId(), key -> new ArrayList<>()).add(option));
        return result;
    }

    private List<KyzzQuestion> selectExamQuestions(ExamComposition composition, String difficultyMode) {
        List<Long> activeBankIds = kyzzQuestionBankMapper.selectList(new LambdaQueryWrapper<KyzzQuestionBank>()
                        .eq(KyzzQuestionBank::getStatus, 1)
                        .orderByAsc(KyzzQuestionBank::getSortNo)
                        .orderByAsc(KyzzQuestionBank::getId))
                .stream()
                .map(KyzzQuestionBank::getId)
                .toList();
        if (activeBankIds.isEmpty()) {
            return List.of();
        }
        List<String> requiredTypes = composition.requiredTypes();
        List<KyzzQuestion> allQuestions = kyzzQuestionMapper.selectList(new LambdaQueryWrapper<KyzzQuestion>()
                .in(KyzzQuestion::getQuestionBankId, activeBankIds)
                .in(KyzzQuestion::getQuestionType, requiredTypes)
                .eq(KyzzQuestion::getStatus, 1)
                .orderByAsc(KyzzQuestion::getQuestionType)
                .orderByAsc(KyzzQuestion::getSortNo)
                .orderByAsc(KyzzQuestion::getId));
        Map<String, List<KyzzQuestion>> questionMap = allQuestions.stream()
                .collect(Collectors.groupingBy(KyzzQuestion::getQuestionType, LinkedHashMap::new, Collectors.toCollection(ArrayList::new)));
        List<KyzzQuestion> selected = new ArrayList<>();
        selected.addAll(takeQuestions(questionMap.getOrDefault(QUESTION_TYPE_SINGLE, List.of()), difficultyMode, composition.singleCount()));
        selected.addAll(takeQuestions(questionMap.getOrDefault(QUESTION_TYPE_MULTIPLE, List.of()), difficultyMode, composition.multipleCount()));
        selected.addAll(takeQuestions(questionMap.getOrDefault(QUESTION_TYPE_SHORT, List.of()), difficultyMode, composition.shortCount()));
        return selected;
    }

    private List<KyzzQuestion> takeQuestions(List<KyzzQuestion> source, String difficultyMode, int targetCount) {
        if (targetCount <= 0 || source.isEmpty()) {
            return List.of();
        }
        List<KyzzQuestion> preferred = source.stream()
                .filter(question -> matchesDifficulty(question.getDifficultyLevel(), difficultyMode))
                .collect(Collectors.toCollection(ArrayList::new));
        if (preferred.size() < targetCount) {
            LinkedHashMap<Long, KyzzQuestion> merged = new LinkedHashMap<>();
            preferred.forEach(question -> merged.put(question.getId(), question));
            source.forEach(question -> merged.putIfAbsent(question.getId(), question));
            preferred = new ArrayList<>(merged.values());
        }
        Collections.shuffle(preferred, ThreadLocalRandom.current());
        return preferred.stream()
                .limit(targetCount)
                .sorted(Comparator.comparing(KyzzQuestion::getQuestionType)
                        .thenComparing(KyzzQuestion::getSortNo, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(KyzzQuestion::getId))
                .toList();
    }

    private boolean matchesDifficulty(Integer difficultyLevel, String difficultyMode) {
        int level = difficultyLevel == null ? 2 : difficultyLevel;
        return switch (difficultyMode) {
            case DIFFICULTY_EASY -> level <= 2;
            case DIFFICULTY_NORMAL -> level >= 2 && level <= 3;
            case DIFFICULTY_HARD -> level >= 3;
            default -> true;
        };
    }

    private String normalizeAnswerContent(String questionType, KyzzExamAnswerSaveRequest request) {
        if (isChoiceQuestion(questionType)) {
            List<String> selectedKeys = normalizeChoiceKeys(request == null ? null : request.getSelectedOptionKeys());
            if (QUESTION_TYPE_SINGLE.equals(questionType) && selectedKeys.size() > 1) {
                throw new BusinessException(ApiResponseCode.BAD_REQUEST, "单选题只能选择一个选项");
            }
            return selectedKeys.isEmpty() ? null : String.join(",", selectedKeys);
        }
        if (QUESTION_TYPE_SHORT.equals(questionType)) {
            String answerText = request == null ? null : request.getAnswerText();
            if (!StringUtils.hasText(answerText)) {
                return null;
            }
            String normalized = answerText.trim();
            if (normalized.length() > 3000) {
                throw new BusinessException(ApiResponseCode.BAD_REQUEST, "简答题答案不能超过3000个字符");
            }
            return normalized;
        }
        throw new BusinessException(ApiResponseCode.BAD_REQUEST, "暂不支持的题型");
    }

    private List<String> normalizeChoiceKeys(List<String> selectedOptionKeys) {
        if (selectedOptionKeys == null || selectedOptionKeys.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String selectedOptionKey : selectedOptionKeys) {
            if (!StringUtils.hasText(selectedOptionKey)) {
                continue;
            }
            String key = selectedOptionKey.trim().toUpperCase(Locale.ROOT);
            if (key.length() > 10) {
                throw new BusinessException(ApiResponseCode.BAD_REQUEST, "选项标识格式不正确");
            }
            result.add(key);
        }
        return result.stream().sorted().toList();
    }

    private List<String> parseChoiceAnswerContent(String answerContent) {
        if (!StringUtils.hasText(answerContent)) {
            return List.of();
        }
        return List.of(answerContent.split(",")).stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private int normalizeUsedSeconds(Integer usedSeconds) {
        if (usedSeconds == null) {
            return 0;
        }
        return Math.max(0, Math.min(usedSeconds, MAX_DURATION_MINUTES * 60));
    }

    private String normalizeExamType(String examType) {
        String value = StringUtils.hasText(examType) ? examType.trim().toLowerCase(Locale.ROOT) : EXAM_TYPE_FULL;
        if (!SUPPORTED_EXAM_TYPES.contains(value)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "考试类型仅支持 choice/short/full");
        }
        return value;
    }

    private String normalizeDifficultyMode(String difficultyMode) {
        String value = StringUtils.hasText(difficultyMode) ? difficultyMode.trim().toLowerCase(Locale.ROOT) : DIFFICULTY_BALANCED;
        if (!SUPPORTED_DIFFICULTY_MODES.contains(value)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "难度仅支持 balanced/easy/normal/hard");
        }
        return value;
    }

    private int normalizeDurationMinutes(Integer durationMinutes, int defaultDurationMinutes) {
        int value = durationMinutes == null ? defaultDurationMinutes : durationMinutes;
        if (value < 10 || value > MAX_DURATION_MINUTES) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "考试时间需保持在 10 到 240 分钟之间");
        }
        return value;
    }

    private ExamComposition resolveComposition(String examType) {
        return switch (examType) {
            case EXAM_TYPE_CHOICE -> new ExamComposition(16, 17, 0, 60);
            case EXAM_TYPE_SHORT -> new ExamComposition(0, 0, 5, 90);
            default -> new ExamComposition(16, 17, 5, 180);
        };
    }

    private List<KyzzExamPresetResponse> buildPresets() {
        return List.of(
                new KyzzExamPresetResponse(
                        EXAM_TYPE_CHOICE,
                        "选择题测验",
                        "按真题选择题结构抽取 16 道单选和 17 道多选。",
                        60,
                        16,
                        17,
                        0
                ),
                new KyzzExamPresetResponse(
                        EXAM_TYPE_SHORT,
                        "简答题测验",
                        "抽取 5 道主观题，适合背诵后完整默写。",
                        90,
                        0,
                        0,
                        5
                ),
                new KyzzExamPresetResponse(
                        EXAM_TYPE_FULL,
                        "完整试卷测验",
                        "按政治真题结构抽取 16 单选、17 多选、5 简答。",
                        180,
                        16,
                        17,
                        5
                )
        );
    }

    private List<KyzzExamDifficultyOptionResponse> buildDifficultyOptions() {
        return List.of(
                new KyzzExamDifficultyOptionResponse(DIFFICULTY_BALANCED, "综合", "从全部可用题目中均衡抽取。"),
                new KyzzExamDifficultyOptionResponse(DIFFICULTY_EASY, "基础", "优先抽取简单和中等题。"),
                new KyzzExamDifficultyOptionResponse(DIFFICULTY_NORMAL, "标准", "优先抽取中等和困难题。"),
                new KyzzExamDifficultyOptionResponse(DIFFICULTY_HARD, "拔高", "优先抽取困难题，不足时自动补齐。")
        );
    }

    private KyzzExamSummaryResponse toSummary(KyzzExamSession session, LocalDateTime now) {
        if (session == null) {
            return null;
        }
        String gradingStatus = normalizeGradingStatus(session.getGradingStatus());
        return new KyzzExamSummaryResponse(
                session.getId(),
                session.getExamNo(),
                session.getExamType(),
                examTypeLabel(session.getExamType()),
                session.getDifficultyMode(),
                difficultyLabel(session.getDifficultyMode()),
                session.getDurationMinutes(),
                session.getTotalQuestionCount(),
                session.getAnsweredCount() == null ? 0 : session.getAnsweredCount(),
                session.getTotalScore(),
                zeroIfNull(session.getEarnedScore()),
                zeroIfNull(session.getObjectiveScore()),
                zeroIfNull(session.getSubjectiveScore()),
                session.getStatus(),
                statusLabel(session.getStatus()),
                gradingStatus,
                gradingStatusLabel(gradingStatus),
                formatTime(session.getStartedAt()),
                formatTime(session.getDeadlineAt()),
                formatTime(session.getSubmittedAt()),
                formatTime(session.getGradedAt()),
                session.getGradingErrorMessage(),
                remainingSeconds(session, now)
        );
    }

    private Long remainingSeconds(KyzzExamSession session, LocalDateTime now) {
        if (!STATUS_IN_PROGRESS.equals(session.getStatus()) || session.getDeadlineAt() == null) {
            return 0L;
        }
        return Math.max(0, Duration.between(now, session.getDeadlineAt()).getSeconds());
    }

    private String examTypeLabel(String examType) {
        return switch (examType) {
            case EXAM_TYPE_CHOICE -> "选择题测验";
            case EXAM_TYPE_SHORT -> "简答题测验";
            case EXAM_TYPE_FULL -> "完整试卷测验";
            default -> "考试";
        };
    }

    private String difficultyLabel(String difficultyMode) {
        return switch (difficultyMode) {
            case DIFFICULTY_EASY -> "基础";
            case DIFFICULTY_NORMAL -> "标准";
            case DIFFICULTY_HARD -> "拔高";
            default -> "综合";
        };
    }

    private String statusLabel(String status) {
        return switch (status) {
            case STATUS_IN_PROGRESS -> "进行中";
            case STATUS_SUBMITTED -> "已交卷";
            case STATUS_EXPIRED -> "已超时";
            default -> "已结束";
        };
    }

    private String gradingStatusLabel(String gradingStatus) {
        return switch (gradingStatus) {
            case GRADING_STATUS_GRADING -> "阅卷中";
            case GRADING_STATUS_GRADED -> "已阅卷";
            case GRADING_STATUS_FAILED -> "阅卷失败";
            default -> "待阅卷";
        };
    }

    private String normalizeGradingStatus(String gradingStatus) {
        return StringUtils.hasText(gradingStatus) ? gradingStatus.trim() : GRADING_STATUS_NOT_STARTED;
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean isChoiceQuestion(String questionType) {
        return QUESTION_TYPE_SINGLE.equals(questionType) || QUESTION_TYPE_MULTIPLE.equals(questionType);
    }

    private List<String> resolveCorrectOptionKeys(List<KyzzQuestionOption> options) {
        if (options == null || options.isEmpty()) {
            return List.of();
        }
        return options.stream()
                .filter(option -> Objects.equals(option.getIsCorrect(), 1))
                .map(KyzzQuestionOption::getOptionKey)
                .filter(StringUtils::hasText)
                .map(item -> item.trim().toUpperCase(Locale.ROOT))
                .sorted()
                .toList();
    }

    private int countByType(List<KyzzQuestion> questions, String questionType) {
        return (int) questions.stream().filter(question -> questionType.equals(question.getQuestionType())).count();
    }

    private BigDecimal calculateTotalScore(List<KyzzQuestion> questions) {
        return questions.stream()
                .map(this::resolveExamScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal resolveExamScore(KyzzQuestion question) {
        return switch (question.getQuestionType()) {
            case QUESTION_TYPE_SINGLE -> BigDecimal.ONE;
            case QUESTION_TYPE_MULTIPLE -> BigDecimal.valueOf(2);
            case QUESTION_TYPE_SHORT -> BigDecimal.TEN;
            default -> question.getScore() == null ? BigDecimal.ONE : question.getScore();
        };
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? null : time.format(DATE_TIME_FORMATTER);
    }

    private String generateExamNo() {
        return "KZ" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    private GradingPoints parseGradingPoints(String gradingPointsJson) {
        if (!StringUtils.hasText(gradingPointsJson)) {
            return GradingPoints.empty();
        }
        try {
            JsonNode root = objectMapper.readTree(gradingPointsJson);
            return new GradingPoints(
                    readStringArray(root.path("matchedPoints")),
                    readStringArray(root.path("missingPoints"))
            );
        } catch (Exception ex) {
            return GradingPoints.empty();
        }
    }

    private List<String> readStringArray(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (JsonNode item : node) {
            String value = item.asText("");
            if (StringUtils.hasText(value)) {
                result.add(value.trim());
            }
        }
        return result;
    }

    private String firstText(String primary, String fallback) {
        if (StringUtils.hasText(primary)) {
            return primary;
        }
        return StringUtils.hasText(fallback) ? fallback : null;
    }

    private void enqueueGradingAfterCommit(Long sessionId) {
        if (sessionId == null) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    kyzzExamGradingService.gradeExamAsync(sessionId);
                }
            });
            return;
        }
        kyzzExamGradingService.gradeExamAsync(sessionId);
    }

    private LinkedHashSet<Long> normalizeIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private record GradingPoints(List<String> matchedPoints, List<String> missingPoints) {

        private static GradingPoints empty() {
            return new GradingPoints(List.of(), List.of());
        }
    }

    private record ExamComposition(int singleCount, int multipleCount, int shortCount, int defaultDurationMinutes) {

        private List<String> requiredTypes() {
            List<String> result = new ArrayList<>();
            if (singleCount > 0) {
                result.add(QUESTION_TYPE_SINGLE);
            }
            if (multipleCount > 0) {
                result.add(QUESTION_TYPE_MULTIPLE);
            }
            if (shortCount > 0) {
                result.add(QUESTION_TYPE_SHORT);
            }
            return result;
        }
    }
}
