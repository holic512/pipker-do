/**
 * @file KyyyReadingUserService
 * @project pipker-do
 * @module 考研英语 / 阅读做题
 * @description 编排阅读篇章恢复、整篇做题、单题存稿、交卷判分与错题沉淀逻辑。
 * @logic 1. 按考试方向恢复或新建阅读会话；2. 单题答案即存即更进度；3. 交卷时校验标准答案一致性并回写解析快照与错题聚合。
 * @dependencies Mapper: KyyyReadingPassageMapper, Mapper: KyyyReadingQuestionMapper, Mapper: KyyyReadingQuestionOptionMapper, Mapper: KyyyReadingSessionMapper, Mapper: KyyyUserReadingAnswerMapper, Mapper: KyyyUserReadingWrongQuestionMapper, Mapper: KyyyUserPracticeSettingMapper, Config: JacksonObjectMapperConfig
 * @index_tags 考研英语, 阅读服务, 整篇作答, 交卷判分
 * @author holic512
 */
package org.example.backend.biz.kyyy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.biz.kyyy.dto.KyyyReadingAnswerDraftRequest;
import org.example.backend.biz.kyyy.dto.KyyyReadingAnnotationResponse;
import org.example.backend.biz.kyyy.dto.KyyyReadingOptionResponse;
import org.example.backend.biz.kyyy.dto.KyyyReadingPassageResponse;
import org.example.backend.biz.kyyy.dto.KyyyReadingProgressResponse;
import org.example.backend.biz.kyyy.dto.KyyyReadingQuestionResponse;
import org.example.backend.biz.kyyy.dto.KyyyReadingSessionResponse;
import org.example.backend.biz.kyyy.dto.KyyyReadingSummaryResponse;
import org.example.backend.biz.kyyy.entity.KyyyReadingPassage;
import org.example.backend.biz.kyyy.entity.KyyyReadingQuestion;
import org.example.backend.biz.kyyy.entity.KyyyReadingQuestionOption;
import org.example.backend.biz.kyyy.entity.KyyyReadingSession;
import org.example.backend.biz.kyyy.entity.KyyyUserPracticeSetting;
import org.example.backend.biz.kyyy.entity.KyyyUserReadingAnswer;
import org.example.backend.biz.kyyy.entity.KyyyUserReadingAnnotation;
import org.example.backend.biz.kyyy.entity.KyyyUserReadingWrongQuestion;
import org.example.backend.biz.kyyy.mapper.KyyyReadingPassageMapper;
import org.example.backend.biz.kyyy.mapper.KyyyReadingQuestionMapper;
import org.example.backend.biz.kyyy.mapper.KyyyReadingQuestionOptionMapper;
import org.example.backend.biz.kyyy.mapper.KyyyReadingSessionMapper;
import org.example.backend.biz.kyyy.mapper.KyyyUserPracticeSettingMapper;
import org.example.backend.biz.kyyy.mapper.KyyyUserReadingAnswerMapper;
import org.example.backend.biz.kyyy.mapper.KyyyUserReadingWrongQuestionMapper;
import org.example.backend.biz.kyyy.support.KyyyExamDirectionSupport;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class KyyyReadingUserService {

    private static final int ACTIVE_STATUS = 1;
    private static final String SESSION_STATUS_ACTIVE = "active";
    private static final String SESSION_STATUS_SUBMITTED = "submitted";
    private static final String SESSION_STATUS_ABANDONED = "abandoned";
    private static final String RESPONSE_STATUS_EMPTY = "empty";
    private static final String ANSWER_STATUS_ANSWERED = "answered";
    private static final String ANSWER_STATUS_SKIPPED = "skipped";
    private static final Set<String> SUPPORTED_QUESTION_TYPES = Set.of("single_choice");

    private final KyyyReadingPassageMapper kyyyReadingPassageMapper;
    private final KyyyReadingQuestionMapper kyyyReadingQuestionMapper;
    private final KyyyReadingQuestionOptionMapper kyyyReadingQuestionOptionMapper;
    private final KyyyReadingSessionMapper kyyyReadingSessionMapper;
    private final KyyyUserReadingAnswerMapper kyyyUserReadingAnswerMapper;
    private final KyyyUserReadingWrongQuestionMapper kyyyUserReadingWrongQuestionMapper;
    private final KyyyUserPracticeSettingMapper kyyyUserPracticeSettingMapper;
    private final KyyyReadingAnnotationUserService kyyyReadingAnnotationUserService;
    private final ObjectMapper objectMapper;

    public KyyyReadingUserService(KyyyReadingPassageMapper kyyyReadingPassageMapper,
                                  KyyyReadingQuestionMapper kyyyReadingQuestionMapper,
                                  KyyyReadingQuestionOptionMapper kyyyReadingQuestionOptionMapper,
                                  KyyyReadingSessionMapper kyyyReadingSessionMapper,
                                  KyyyUserReadingAnswerMapper kyyyUserReadingAnswerMapper,
                                  KyyyUserReadingWrongQuestionMapper kyyyUserReadingWrongQuestionMapper,
                                  KyyyUserPracticeSettingMapper kyyyUserPracticeSettingMapper,
                                  KyyyReadingAnnotationUserService kyyyReadingAnnotationUserService,
                                  ObjectMapper objectMapper) {
        this.kyyyReadingPassageMapper = kyyyReadingPassageMapper;
        this.kyyyReadingQuestionMapper = kyyyReadingQuestionMapper;
        this.kyyyReadingQuestionOptionMapper = kyyyReadingQuestionOptionMapper;
        this.kyyyReadingSessionMapper = kyyyReadingSessionMapper;
        this.kyyyUserReadingAnswerMapper = kyyyUserReadingAnswerMapper;
        this.kyyyUserReadingWrongQuestionMapper = kyyyUserReadingWrongQuestionMapper;
        this.kyyyUserPracticeSettingMapper = kyyyUserPracticeSettingMapper;
        this.kyyyReadingAnnotationUserService = kyyyReadingAnnotationUserService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public KyyyReadingSessionResponse getSession(Long userId, Long passageId, Boolean freshAttempt) {
        String examDirection = loadExamDirection(userId);
        KyyyReadingPassage targetPassage = passageId == null ? null : requireActivePassage(passageId, examDirection);
        if (Boolean.TRUE.equals(freshAttempt)) {
            abandonActiveSessions(userId, examDirection);
        }

        if (passageId != null) {
            KyyyReadingSession activeSession = loadLatestActiveSession(userId, examDirection, passageId);
            if (activeSession == null) {
                activeSession = createSession(userId, examDirection, targetPassage);
            }
            return buildSessionResponse(activeSession, targetPassage);
        }

        KyyyReadingSession activeSession = loadLatestActiveSession(userId, examDirection, null);
        if (activeSession != null) {
            return buildSessionResponse(activeSession, requireSessionPassage(activeSession, examDirection));
        }

        KyyyReadingPassage defaultPassage = loadDefaultPassage(examDirection);
        if (defaultPassage == null) {
            return buildEmptyResponse();
        }
        return buildSessionResponse(createSession(userId, examDirection, defaultPassage), defaultPassage);
    }

    @Transactional
    public KyyyReadingSessionResponse saveAnswerDraft(Long userId,
                                                      Long sessionId,
                                                      Long questionId,
                                                      KyyyReadingAnswerDraftRequest request) {
        if (request == null || !StringUtils.hasText(request.getAnswerContent())) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "请选择当前题答案");
        }
        KyyyReadingSession session = requireOwnedSession(userId, sessionId);
        String examDirection = loadExamDirection(userId);
        KyyyReadingPassage passage = requireSessionPassage(session, session.getExamDirectionSnapshot());
        if (SESSION_STATUS_SUBMITTED.equals(session.getSessionStatus())) {
            return buildSessionResponse(session, passage);
        }
        if (!SESSION_STATUS_ACTIVE.equals(session.getSessionStatus())) {
            throw new BusinessException(ApiResponseCode.CONFLICT, "当前阅读会话已失效，请重新进入");
        }
        if (!Objects.equals(session.getExamDirectionSnapshot(), examDirection)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前阅读会话与已选择考试方向不一致，请重新进入");
        }

        ReadingMaterial material = loadReadingMaterial(passage.getId());
        QuestionContext questionContext = material.questionContextMap().get(questionId);
        if (questionContext == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前题目不属于该阅读会话");
        }

        String answerContent = normalizeAnswerKey(request.getAnswerContent());
        if (!questionContext.availableOptionKeys().contains(answerContent)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前题目答案仅支持 A/B/C/D 选项");
        }

        KyyyUserReadingAnswer existingAnswer = loadSessionAnswer(sessionId, questionId);
        LocalDateTime now = LocalDateTime.now();
        String optionSnapshotJson = serializeOptionSnapshot(questionContext.optionResponses());
        int usedSeconds = Math.max(
                safeInt(existingAnswer == null ? null : existingAnswer.getUsedSeconds()),
                Math.max(safeInt(request.getUsedSeconds()), 0)
        );
        if (existingAnswer == null) {
            KyyyUserReadingAnswer answer = new KyyyUserReadingAnswer();
            answer.setSessionId(sessionId);
            answer.setUserId(userId);
            answer.setPassageId(passage.getId());
            answer.setQuestionId(questionId);
            answer.setQuestionNoSnapshot(questionContext.question().getQuestionNo());
            answer.setQuestionStemSnapshot(questionContext.question().getStem());
            answer.setOptionSnapshotJson(optionSnapshotJson);
            answer.setAnswerContent(answerContent);
            answer.setAnswerStatus(ANSWER_STATUS_ANSWERED);
            answer.setUsedSeconds(usedSeconds);
            answer.setSubmittedAt(now);
            kyyyUserReadingAnswerMapper.insert(answer);
        } else {
            existingAnswer.setQuestionNoSnapshot(questionContext.question().getQuestionNo());
            existingAnswer.setQuestionStemSnapshot(questionContext.question().getStem());
            existingAnswer.setOptionSnapshotJson(optionSnapshotJson);
            existingAnswer.setAnswerContent(answerContent);
            existingAnswer.setAnswerStatus(ANSWER_STATUS_ANSWERED);
            existingAnswer.setUsedSeconds(usedSeconds);
            existingAnswer.setSubmittedAt(now);
            existingAnswer.setUpdatedAt(now);
            kyyyUserReadingAnswerMapper.updateById(existingAnswer);
        }

        int answeredCount = countAnsweredQuestions(sessionId);
        session.setAnsweredCount(answeredCount);
        session.setUpdatedAt(now);
        kyyyReadingSessionMapper.updateById(session);
        return buildSessionResponse(session, passage);
    }

    @Transactional
    public KyyyReadingSessionResponse submitSession(Long userId, Long sessionId) {
        KyyyReadingSession session = requireOwnedSession(userId, sessionId);
        KyyyReadingPassage passage = requireSessionPassage(session, session.getExamDirectionSnapshot());
        if (SESSION_STATUS_SUBMITTED.equals(session.getSessionStatus())) {
            return buildSessionResponse(session, passage);
        }
        if (!SESSION_STATUS_ACTIVE.equals(session.getSessionStatus())) {
            throw new BusinessException(ApiResponseCode.CONFLICT, "当前阅读会话已失效，请重新进入");
        }

        ReadingMaterial material = loadReadingMaterial(passage.getId());
        Map<Long, KyyyUserReadingAnswer> answerMap = loadAnswerMap(sessionId, material.questionIds());
        LocalDateTime now = LocalDateTime.now();
        int totalQuestionCount = material.questionContexts().size();
        int answeredCount = 0;
        int correctCount = 0;
        int wrongCount = 0;

        for (QuestionContext questionContext : material.questionContexts()) {
            String correctAnswer = resolveCorrectAnswer(questionContext);
            KyyyUserReadingAnswer answer = answerMap.get(questionContext.question().getId());
            String normalizedAnswer = normalizeAnswerKey(answer == null ? null : answer.getAnswerContent());
            boolean answered = StringUtils.hasText(normalizedAnswer);
            boolean correct = answered && Objects.equals(normalizedAnswer, correctAnswer);
            if (answered) {
                answeredCount++;
            }
            if (correct) {
                correctCount++;
            } else {
                wrongCount++;
            }

            String optionSnapshotJson = serializeOptionSnapshot(questionContext.optionResponses());
            if (answer == null) {
                answer = new KyyyUserReadingAnswer();
                answer.setSessionId(sessionId);
                answer.setUserId(userId);
                answer.setPassageId(passage.getId());
                answer.setQuestionId(questionContext.question().getId());
                answer.setUsedSeconds(0);
                answer.setCreatedAt(now);
            }
            answer.setQuestionNoSnapshot(questionContext.question().getQuestionNo());
            answer.setQuestionStemSnapshot(questionContext.question().getStem());
            answer.setOptionSnapshotJson(optionSnapshotJson);
            answer.setAnswerContent(answered ? normalizedAnswer : null);
            answer.setCorrectAnswerSnapshot(correctAnswer);
            answer.setAnalysisSnapshot(questionContext.question().getAnalysis());
            answer.setIsCorrect(correct ? 1 : 0);
            answer.setAnswerStatus(answered ? ANSWER_STATUS_ANSWERED : ANSWER_STATUS_SKIPPED);
            answer.setSubmittedAt(now);
            answer.setUpdatedAt(now);
            if (answer.getId() == null) {
                kyyyUserReadingAnswerMapper.insert(answer);
            } else {
                kyyyUserReadingAnswerMapper.updateById(answer);
            }

            syncWrongQuestion(userId, passage.getId(), questionContext.question().getId(), correct, now);
        }

        session.setTotalQuestionCount(totalQuestionCount);
        session.setAnsweredCount(answeredCount);
        session.setCorrectCount(correctCount);
        session.setWrongCount(wrongCount);
        session.setAccuracyRate(calculateAccuracyRate(correctCount, totalQuestionCount));
        session.setSessionStatus(SESSION_STATUS_SUBMITTED);
        session.setSubmittedAt(now);
        session.setUpdatedAt(now);
        kyyyReadingSessionMapper.updateById(session);
        return buildSessionResponse(session, passage);
    }

    private KyyyReadingSessionResponse buildSessionResponse(KyyyReadingSession session, KyyyReadingPassage passage) {
        ReadingMaterial material = loadReadingMaterial(passage.getId());
        Map<Long, KyyyUserReadingAnswer> answerMap = session == null
                ? Map.of()
                : loadAnswerMap(session.getId(), material.questionIds());
        AnnotationResponseGroup annotationGroup = session == null
                ? new AnnotationResponseGroup(List.of(), Map.of())
                : buildAnnotationResponseGroup(session.getUserId(), passage, material.questions());
        boolean submitted = session != null && SESSION_STATUS_SUBMITTED.equals(session.getSessionStatus());
        int totalQuestionCount = material.questionContexts().size();
        int answeredCount = session == null ? 0 : safeInt(session.getAnsweredCount());
        int unansweredCount = Math.max(totalQuestionCount - answeredCount, 0);
        List<KyyyReadingQuestionResponse> questions = new ArrayList<>();
        for (QuestionContext questionContext : material.questionContexts()) {
            KyyyUserReadingAnswer answer = answerMap.get(questionContext.question().getId());
            questions.add(new KyyyReadingQuestionResponse(
                    questionContext.question().getId(),
                    questionContext.question().getQuestionNo(),
                    questionContext.question().getStem(),
                    questionContext.optionResponses(),
                    normalizeAnswerKey(answer == null ? null : answer.getAnswerContent()),
                    normalizeAnswerStatus(answer == null ? null : answer.getAnswerStatus()),
                    submitted && answer != null ? Integer.valueOf(1).equals(answer.getIsCorrect()) : null,
                    submitted ? normalizeAnswerKey(answer == null ? null : answer.getCorrectAnswerSnapshot()) : null,
                    submitted ? safeText(answer == null ? null : answer.getAnalysisSnapshot()) : null,
                    annotationGroup.questionAnnotationsMap().getOrDefault(questionContext.question().getId(), List.of())
            ));
        }

        return new KyyyReadingSessionResponse(
                session == null ? null : session.getId(),
                session == null ? RESPONSE_STATUS_EMPTY : normalizeSessionStatus(session.getSessionStatus()),
                new KyyyReadingPassageResponse(
                        passage.getId(),
                        passage.getSourceYear(),
                        passage.getSourceName(),
                        passage.getPassageNo(),
                        passage.getTitle(),
                        passage.getPassageText(),
                        passage.getExamDirection(),
                        KyyyExamDirectionSupport.labelOf(passage.getExamDirection()),
                        annotationGroup.passageAnnotations()
                ),
                new KyyyReadingProgressResponse(
                        totalQuestionCount,
                        answeredCount,
                        unansweredCount,
                        submitted
                ),
                questions,
                submitted
                        ? new KyyyReadingSummaryResponse(
                        safeInt(session.getCorrectCount()),
                        safeInt(session.getWrongCount()),
                        session.getAccuracyRate() == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : session.getAccuracyRate(),
                        session.getSubmittedAt()
                )
                        : null
        );
    }

    private KyyyReadingSessionResponse buildEmptyResponse() {
        return new KyyyReadingSessionResponse(
                null,
                RESPONSE_STATUS_EMPTY,
                null,
                new KyyyReadingProgressResponse(0, 0, 0, false),
                List.of(),
                null
        );
    }

    private KyyyReadingSession requireOwnedSession(Long userId, Long sessionId) {
        KyyyReadingSession session = kyyyReadingSessionMapper.selectById(sessionId);
        if (session == null || !Objects.equals(session.getUserId(), userId)) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "阅读会话不存在");
        }
        return session;
    }

    private KyyyReadingPassage requireActivePassage(Long passageId, String examDirection) {
        KyyyReadingPassage passage = kyyyReadingPassageMapper.selectById(passageId);
        if (passage == null || !Objects.equals(passage.getStatus(), ACTIVE_STATUS)) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "当前阅读文章不存在");
        }
        if (!Objects.equals(KyyyExamDirectionSupport.normalizeOrDefault(passage.getExamDirection()), examDirection)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前阅读文章不属于已选择考试方向");
        }
        return passage;
    }

    private KyyyReadingPassage requireSessionPassage(KyyyReadingSession session, String examDirection) {
        KyyyReadingPassage passage = kyyyReadingPassageMapper.selectById(session.getPassageId());
        if (passage == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "阅读文章不存在");
        }
        if (!Objects.equals(KyyyExamDirectionSupport.normalizeOrDefault(passage.getExamDirection()), examDirection)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前阅读文章与会话考试方向不一致");
        }
        return passage;
    }

    private KyyyReadingPassage loadDefaultPassage(String examDirection) {
        return kyyyReadingPassageMapper.selectOne(new LambdaQueryWrapper<KyyyReadingPassage>()
                .eq(KyyyReadingPassage::getExamDirection, examDirection)
                .eq(KyyyReadingPassage::getStatus, ACTIVE_STATUS)
                .orderByDesc(KyyyReadingPassage::getSourceYear)
                .orderByAsc(KyyyReadingPassage::getPassageNo)
                .orderByDesc(KyyyReadingPassage::getId)
                .last("limit 1"));
    }

    private KyyyReadingSession loadLatestActiveSession(Long userId, String examDirection, Long passageId) {
        LambdaQueryWrapper<KyyyReadingSession> wrapper = new LambdaQueryWrapper<KyyyReadingSession>()
                .eq(KyyyReadingSession::getUserId, userId)
                .eq(KyyyReadingSession::getExamDirectionSnapshot, examDirection)
                .eq(KyyyReadingSession::getSessionStatus, SESSION_STATUS_ACTIVE)
                .orderByDesc(KyyyReadingSession::getStartedAt)
                .orderByDesc(KyyyReadingSession::getId)
                .last("limit 1");
        if (passageId != null) {
            wrapper.eq(KyyyReadingSession::getPassageId, passageId);
        }
        return kyyyReadingSessionMapper.selectOne(wrapper);
    }

    private void abandonActiveSessions(Long userId, String examDirection) {
        LocalDateTime now = LocalDateTime.now();
        kyyyReadingSessionMapper.update(null, new LambdaUpdateWrapper<KyyyReadingSession>()
                .eq(KyyyReadingSession::getUserId, userId)
                .eq(KyyyReadingSession::getExamDirectionSnapshot, examDirection)
                .eq(KyyyReadingSession::getSessionStatus, SESSION_STATUS_ACTIVE)
                .set(KyyyReadingSession::getSessionStatus, SESSION_STATUS_ABANDONED)
                .set(KyyyReadingSession::getUpdatedAt, now));
    }

    private KyyyReadingSession createSession(Long userId, String examDirection, KyyyReadingPassage passage) {
        ReadingMaterial material = loadReadingMaterial(passage.getId());
        LocalDateTime now = LocalDateTime.now();
        KyyyReadingSession session = new KyyyReadingSession();
        session.setUserId(userId);
        session.setPassageId(passage.getId());
        session.setExamDirectionSnapshot(examDirection);
        session.setSessionStatus(SESSION_STATUS_ACTIVE);
        session.setTotalQuestionCount(material.questionContexts().size());
        session.setAnsweredCount(0);
        session.setCorrectCount(0);
        session.setWrongCount(0);
        session.setAccuracyRate(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        session.setStartedAt(now);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        kyyyReadingSessionMapper.insert(session);
        return session;
    }

    private ReadingMaterial loadReadingMaterial(Long passageId) {
        List<KyyyReadingQuestion> questions = kyyyReadingQuestionMapper.selectList(new LambdaQueryWrapper<KyyyReadingQuestion>()
                .eq(KyyyReadingQuestion::getPassageId, passageId)
                .eq(KyyyReadingQuestion::getStatus, ACTIVE_STATUS)
                .orderByAsc(KyyyReadingQuestion::getSortNo)
                .orderByAsc(KyyyReadingQuestion::getQuestionNo)
                .orderByAsc(KyyyReadingQuestion::getId));
        if (questions.isEmpty()) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "当前阅读暂无可作答题目");
        }

        List<Long> questionIds = questions.stream().map(KyyyReadingQuestion::getId).toList();
        List<KyyyReadingQuestionOption> options = kyyyReadingQuestionOptionMapper.selectList(new LambdaQueryWrapper<KyyyReadingQuestionOption>()
                .in(KyyyReadingQuestionOption::getQuestionId, questionIds)
                .orderByAsc(KyyyReadingQuestionOption::getQuestionId)
                .orderByAsc(KyyyReadingQuestionOption::getSortNo)
                .orderByAsc(KyyyReadingQuestionOption::getId));
        Map<Long, List<KyyyReadingQuestionOption>> optionMap = new LinkedHashMap<>();
        for (KyyyReadingQuestionOption option : options) {
            optionMap.computeIfAbsent(option.getQuestionId(), key -> new ArrayList<>()).add(option);
        }

        List<QuestionContext> questionContexts = new ArrayList<>();
        Map<Long, QuestionContext> questionContextMap = new LinkedHashMap<>();
        for (KyyyReadingQuestion question : questions) {
            String questionType = safeText(question.getQuestionType());
            if (!SUPPORTED_QUESTION_TYPES.contains(questionType)) {
                throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前阅读包含暂不支持的题型");
            }
            List<KyyyReadingQuestionOption> questionOptions = optionMap.getOrDefault(question.getId(), List.of());
            if (questionOptions.isEmpty()) {
                throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前阅读题目缺少选项配置");
            }
            List<KyyyReadingOptionResponse> optionResponses = questionOptions.stream()
                    .map(item -> new KyyyReadingOptionResponse(
                            safeText(item.getOptionKey()),
                            safeText(item.getOptionContent())
                    ))
                    .toList();
            Set<String> availableOptionKeys = optionResponses.stream()
                    .map(KyyyReadingOptionResponse::getOptionKey)
                    .filter(StringUtils::hasText)
                    .map(this::normalizeAnswerKey)
                    .filter(StringUtils::hasText)
                    .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
            QuestionContext questionContext = new QuestionContext(question, questionOptions, optionResponses, availableOptionKeys);
            questionContexts.add(questionContext);
            questionContextMap.put(question.getId(), questionContext);
        }
        return new ReadingMaterial(questionIds, questions, questionContexts, questionContextMap);
    }

    private AnnotationResponseGroup buildAnnotationResponseGroup(Long userId,
                                                                 KyyyReadingPassage passage,
                                                                 List<KyyyReadingQuestion> questions) {
        List<KyyyUserReadingAnnotation> annotations = kyyyReadingAnnotationUserService.loadValidActiveAnnotations(userId, passage, questions);
        if (annotations.isEmpty()) {
            return new AnnotationResponseGroup(List.of(), Map.of());
        }
        List<KyyyReadingAnnotationResponse> passageAnnotations = new ArrayList<>();
        Map<Long, List<KyyyReadingAnnotationResponse>> questionAnnotationsMap = new LinkedHashMap<>();
        for (KyyyUserReadingAnnotation annotation : annotations) {
            KyyyReadingAnnotationResponse response = kyyyReadingAnnotationUserService.toResponse(annotation);
            if (annotation.getQuestionId() == null) {
                passageAnnotations.add(response);
                continue;
            }
            questionAnnotationsMap.computeIfAbsent(annotation.getQuestionId(), key -> new ArrayList<>()).add(response);
        }
        return new AnnotationResponseGroup(passageAnnotations, questionAnnotationsMap);
    }

    private Map<Long, KyyyUserReadingAnswer> loadAnswerMap(Long sessionId, List<Long> questionIds) {
        if (sessionId == null || questionIds == null || questionIds.isEmpty()) {
            return Map.of();
        }
        List<KyyyUserReadingAnswer> answers = kyyyUserReadingAnswerMapper.selectList(new LambdaQueryWrapper<KyyyUserReadingAnswer>()
                .eq(KyyyUserReadingAnswer::getSessionId, sessionId)
                .in(KyyyUserReadingAnswer::getQuestionId, questionIds)
                .orderByDesc(KyyyUserReadingAnswer::getUpdatedAt)
                .orderByDesc(KyyyUserReadingAnswer::getId));
        Map<Long, KyyyUserReadingAnswer> answerMap = new LinkedHashMap<>();
        for (KyyyUserReadingAnswer answer : answers) {
            answerMap.putIfAbsent(answer.getQuestionId(), answer);
        }
        return answerMap;
    }

    private KyyyUserReadingAnswer loadSessionAnswer(Long sessionId, Long questionId) {
        return kyyyUserReadingAnswerMapper.selectOne(new LambdaQueryWrapper<KyyyUserReadingAnswer>()
                .eq(KyyyUserReadingAnswer::getSessionId, sessionId)
                .eq(KyyyUserReadingAnswer::getQuestionId, questionId)
                .last("limit 1"));
    }

    private int countAnsweredQuestions(Long sessionId) {
        Long count = kyyyUserReadingAnswerMapper.selectCount(new LambdaQueryWrapper<KyyyUserReadingAnswer>()
                .eq(KyyyUserReadingAnswer::getSessionId, sessionId)
                .eq(KyyyUserReadingAnswer::getAnswerStatus, ANSWER_STATUS_ANSWERED));
        return count == null ? 0 : count.intValue();
    }

    private void syncWrongQuestion(Long userId, Long passageId, Long questionId, boolean correct, LocalDateTime now) {
        KyyyUserReadingWrongQuestion wrongQuestion = kyyyUserReadingWrongQuestionMapper.selectOne(
                new LambdaQueryWrapper<KyyyUserReadingWrongQuestion>()
                        .eq(KyyyUserReadingWrongQuestion::getUserId, userId)
                        .eq(KyyyUserReadingWrongQuestion::getQuestionId, questionId)
                        .last("limit 1")
        );
        if (correct) {
            if (wrongQuestion == null) {
                return;
            }
            wrongQuestion.setPassageId(passageId);
            wrongQuestion.setIsMastered(1);
            wrongQuestion.setMasteredAt(now);
            wrongQuestion.setUpdatedAt(now);
            kyyyUserReadingWrongQuestionMapper.updateById(wrongQuestion);
            return;
        }
        if (wrongQuestion == null) {
            wrongQuestion = new KyyyUserReadingWrongQuestion();
            wrongQuestion.setUserId(userId);
            wrongQuestion.setPassageId(passageId);
            wrongQuestion.setQuestionId(questionId);
            wrongQuestion.setFirstWrongAt(now);
            wrongQuestion.setLastWrongAt(now);
            wrongQuestion.setWrongCount(1);
            wrongQuestion.setIsMastered(0);
            wrongQuestion.setCreatedAt(now);
            wrongQuestion.setUpdatedAt(now);
            kyyyUserReadingWrongQuestionMapper.insert(wrongQuestion);
            return;
        }
        wrongQuestion.setPassageId(passageId);
        wrongQuestion.setLastWrongAt(now);
        wrongQuestion.setWrongCount(safeInt(wrongQuestion.getWrongCount()) + 1);
        wrongQuestion.setIsMastered(0);
        wrongQuestion.setMasteredAt(null);
        wrongQuestion.setUpdatedAt(now);
        kyyyUserReadingWrongQuestionMapper.updateById(wrongQuestion);
    }

    private String loadExamDirection(Long userId) {
        KyyyUserPracticeSetting setting = kyyyUserPracticeSettingMapper.selectOne(new LambdaQueryWrapper<KyyyUserPracticeSetting>()
                .eq(KyyyUserPracticeSetting::getUserId, userId)
                .last("limit 1"));
        return KyyyExamDirectionSupport.normalizeOrDefault(setting == null ? null : setting.getExamDirection());
    }

    private String resolveCorrectAnswer(QuestionContext questionContext) {
        String answerText = normalizeAnswerKey(questionContext.question().getAnswerText());
        if (!StringUtils.hasText(answerText)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前阅读题目缺少标准答案");
        }
        String optionMarkedAnswer = "";
        int markedCorrectCount = 0;
        for (KyyyReadingQuestionOption option : questionContext.options()) {
            if (Integer.valueOf(1).equals(option.getIsCorrect())) {
                optionMarkedAnswer = normalizeAnswerKey(option.getOptionKey());
                markedCorrectCount++;
            }
        }
        if (markedCorrectCount != 1) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前阅读题目正确选项配置异常");
        }
        if (!Objects.equals(answerText, optionMarkedAnswer)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前阅读题目标准答案与选项标记不一致");
        }
        if (!questionContext.availableOptionKeys().contains(answerText)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前阅读题目标准答案不在选项范围内");
        }
        return answerText;
    }

    private BigDecimal calculateAccuracyRate(int correctCount, int totalQuestionCount) {
        if (totalQuestionCount <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(correctCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalQuestionCount), 2, RoundingMode.HALF_UP);
    }

    private String serializeOptionSnapshot(List<KyyyReadingOptionResponse> options) {
        try {
            return objectMapper.writeValueAsString(options == null ? List.of() : options);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ApiResponseCode.INTERNAL_ERROR, "阅读题选项快照生成失败");
        }
    }

    private String normalizeSessionStatus(String value) {
        if (SESSION_STATUS_SUBMITTED.equals(value)) {
            return SESSION_STATUS_SUBMITTED;
        }
        if (SESSION_STATUS_ACTIVE.equals(value)) {
            return SESSION_STATUS_ACTIVE;
        }
        return RESPONSE_STATUS_EMPTY;
    }

    private String normalizeAnswerStatus(String value) {
        if (ANSWER_STATUS_SKIPPED.equals(value)) {
            return ANSWER_STATUS_SKIPPED;
        }
        if (ANSWER_STATUS_ANSWERED.equals(value)) {
            return ANSWER_STATUS_ANSWERED;
        }
        return "";
    }

    private String normalizeAnswerKey(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        normalized = normalized.replace("选项", "");
        normalized = normalized.replaceAll("[^A-D]", "");
        if (!StringUtils.hasText(normalized)) {
            return "";
        }
        return String.valueOf(normalized.charAt(0));
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : Math.max(value, 0);
    }

    private record ReadingMaterial(List<Long> questionIds,
                                   List<KyyyReadingQuestion> questions,
                                   List<QuestionContext> questionContexts,
                                   Map<Long, QuestionContext> questionContextMap) {
    }

    private record QuestionContext(KyyyReadingQuestion question,
                                   List<KyyyReadingQuestionOption> options,
                                   List<KyyyReadingOptionResponse> optionResponses,
                                   Set<String> availableOptionKeys) {
    }

    private record AnnotationResponseGroup(List<KyyyReadingAnnotationResponse> passageAnnotations,
                                           Map<Long, List<KyyyReadingAnnotationResponse>> questionAnnotationsMap) {
    }
}
