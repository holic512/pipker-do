package org.example.backend.biz.kyzz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.biz.kyzz.entity.KyzzExamQuestion;
import org.example.backend.biz.kyzz.entity.KyzzExamSession;
import org.example.backend.biz.kyzz.entity.KyzzQuestion;
import org.example.backend.biz.kyzz.entity.KyzzQuestionOption;
import org.example.backend.biz.kyzz.entity.KyzzQuestionTagRel;
import org.example.backend.biz.kyzz.mapper.KyzzExamQuestionMapper;
import org.example.backend.biz.kyzz.mapper.KyzzExamSessionMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionOptionMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionTagRelMapper;
import org.example.backend.shared.llm.dto.LlmGenerateResult;
import org.example.backend.shared.llm.service.LlmService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI 索引: KYZZ 考试选择题和简答题异步判卷服务。
 */
@Service
public class KyzzExamGradingService {

    private static final String QUESTION_TYPE_SINGLE = "single";
    private static final String QUESTION_TYPE_MULTIPLE = "multiple";
    private static final String QUESTION_TYPE_SHORT = "short";
    private static final String STATUS_IN_PROGRESS = "in_progress";
    private static final String GRADING_STATUS_GRADING = "grading";
    private static final String GRADING_STATUS_GRADED = "graded";
    private static final String GRADING_STATUS_FAILED = "failed";
    private static final String GRADING_METHOD_OBJECTIVE = "objective";
    private static final String GRADING_METHOD_LLM = "llm";
    private static final String GRADING_METHOD_BLANK = "blank";
    private static final String LLM_SCENE = "kyzz-exam-short-grading";
    private static final int MAX_REFERENCE_COUNT = 4;
    private static final String SHORT_GRADING_SCHEMA = """
            {
              "type": "object",
              "additionalProperties": false,
              "properties": {
                "awardedScore": { "type": "number", "description": "0 到本题满分之间的得分" },
                "confidence": { "type": "number", "description": "0 到 1 之间的评分置信度" },
                "comment": { "type": "string", "description": "给学生看的简短中文评语" },
                "matchedPoints": {
                  "type": "array",
                  "items": { "type": "string" },
                  "description": "学生答案命中的评分点"
                },
                "missingPoints": {
                  "type": "array",
                  "items": { "type": "string" },
                  "description": "学生答案缺失或表达不足的评分点"
                }
              },
              "required": ["awardedScore", "confidence", "comment", "matchedPoints", "missingPoints"]
            }
            """;

    private final KyzzExamSessionMapper kyzzExamSessionMapper;
    private final KyzzExamQuestionMapper kyzzExamQuestionMapper;
    private final KyzzQuestionMapper kyzzQuestionMapper;
    private final KyzzQuestionOptionMapper kyzzQuestionOptionMapper;
    private final KyzzQuestionTagRelMapper kyzzQuestionTagRelMapper;
    private final LlmService llmService;
    private final ObjectMapper objectMapper;

    public KyzzExamGradingService(KyzzExamSessionMapper kyzzExamSessionMapper,
                                  KyzzExamQuestionMapper kyzzExamQuestionMapper,
                                  KyzzQuestionMapper kyzzQuestionMapper,
                                  KyzzQuestionOptionMapper kyzzQuestionOptionMapper,
                                  KyzzQuestionTagRelMapper kyzzQuestionTagRelMapper,
                                  LlmService llmService,
                                  ObjectMapper objectMapper) {
        this.kyzzExamSessionMapper = kyzzExamSessionMapper;
        this.kyzzExamQuestionMapper = kyzzExamQuestionMapper;
        this.kyzzQuestionMapper = kyzzQuestionMapper;
        this.kyzzQuestionOptionMapper = kyzzQuestionOptionMapper;
        this.kyzzQuestionTagRelMapper = kyzzQuestionTagRelMapper;
        this.llmService = llmService;
        this.objectMapper = objectMapper;
    }

    @Async("kyzzExamGradingTaskExecutor")
    public void gradeExamAsync(Long sessionId) {
        if (sessionId == null) {
            return;
        }
        try {
            gradeExam(sessionId);
        } catch (Exception ex) {
            markSessionFailed(sessionId, ex.getMessage());
        }
    }

    private void gradeExam(Long sessionId) {
        LocalDateTime startedAt = LocalDateTime.now();
        int locked = kyzzExamSessionMapper.update(null, new LambdaUpdateWrapper<KyzzExamSession>()
                .eq(KyzzExamSession::getId, sessionId)
                .ne(KyzzExamSession::getStatus, STATUS_IN_PROGRESS)
                .eq(KyzzExamSession::getGradingStatus, GRADING_STATUS_GRADING)
                .isNull(KyzzExamSession::getGradingStartedAt)
                .set(KyzzExamSession::getGradingStartedAt, startedAt)
                .set(KyzzExamSession::getGradingErrorMessage, null));
        if (locked <= 0) {
            return;
        }

        KyzzExamSession session = kyzzExamSessionMapper.selectById(sessionId);
        if (session == null || STATUS_IN_PROGRESS.equals(session.getStatus())) {
            return;
        }

        List<KyzzExamQuestion> examQuestions = kyzzExamQuestionMapper.selectList(new LambdaQueryWrapper<KyzzExamQuestion>()
                .eq(KyzzExamQuestion::getSessionId, sessionId)
                .orderByAsc(KyzzExamQuestion::getQuestionOrder)
                .orderByAsc(KyzzExamQuestion::getId));
        Map<Long, KyzzQuestion> questionMap = loadQuestionMap(examQuestions.stream().map(KyzzExamQuestion::getQuestionId).toList());
        Map<Long, List<KyzzQuestionOption>> optionMap = loadOptionMap(questionMap.keySet());

        BigDecimal objectiveScore = BigDecimal.ZERO;
        BigDecimal subjectiveScore = BigDecimal.ZERO;
        for (KyzzExamQuestion examQuestion : examQuestions) {
            KyzzQuestion question = questionMap.get(examQuestion.getQuestionId());
            if (question == null) {
                markQuestionFailed(examQuestion, null, "考试题目已失效");
                throw new IllegalStateException("考试题目已失效");
            }
            QuestionGrade grade = gradeQuestion(examQuestion, question, optionMap.getOrDefault(question.getId(), List.of()));
            if (grade.objective()) {
                objectiveScore = objectiveScore.add(grade.awardedScore());
            } else {
                subjectiveScore = subjectiveScore.add(grade.awardedScore());
            }
        }

        BigDecimal normalizedObjective = normalizeMoney(objectiveScore);
        BigDecimal normalizedSubjective = normalizeMoney(subjectiveScore);
        BigDecimal earnedScore = normalizeMoney(normalizedObjective.add(normalizedSubjective));
        LocalDateTime gradedAt = LocalDateTime.now();
        kyzzExamSessionMapper.update(null, new LambdaUpdateWrapper<KyzzExamSession>()
                .eq(KyzzExamSession::getId, sessionId)
                .eq(KyzzExamSession::getGradingStatus, GRADING_STATUS_GRADING)
                .set(KyzzExamSession::getEarnedScore, earnedScore)
                .set(KyzzExamSession::getObjectiveScore, normalizedObjective)
                .set(KyzzExamSession::getSubjectiveScore, normalizedSubjective)
                .set(KyzzExamSession::getGradingStatus, GRADING_STATUS_GRADED)
                .set(KyzzExamSession::getGradedAt, gradedAt)
                .set(KyzzExamSession::getGradingErrorMessage, null));
    }

    private QuestionGrade gradeQuestion(KyzzExamQuestion examQuestion,
                                        KyzzQuestion question,
                                        List<KyzzQuestionOption> options) {
        if (isChoiceQuestion(examQuestion.getQuestionType())) {
            return gradeObjectiveQuestion(examQuestion, question, options);
        }
        if (QUESTION_TYPE_SHORT.equals(examQuestion.getQuestionType())) {
            return gradeShortQuestion(examQuestion, question);
        }
        markQuestionFailed(examQuestion, question, "暂不支持的考试题型");
        throw new IllegalStateException("暂不支持的考试题型");
    }

    private QuestionGrade gradeObjectiveQuestion(KyzzExamQuestion examQuestion,
                                                 KyzzQuestion question,
                                                 List<KyzzQuestionOption> options) {
        List<String> submittedKeys = parseStoredOptionKeys(examQuestion.getAnswerContent());
        List<String> correctKeys = resolveCorrectOptionKeys(options);
        boolean correct = !submittedKeys.isEmpty() && submittedKeys.equals(correctKeys);
        BigDecimal awardedScore = correct ? normalizeMoney(examQuestion.getScore()) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        LocalDateTime gradedAt = LocalDateTime.now();
        kyzzExamQuestionMapper.update(null, new LambdaUpdateWrapper<KyzzExamQuestion>()
                .eq(KyzzExamQuestion::getId, examQuestion.getId())
                .set(KyzzExamQuestion::getAwardedScore, awardedScore)
                .set(KyzzExamQuestion::getIsCorrect, correct ? 1 : 0)
                .set(KyzzExamQuestion::getGradingStatus, GRADING_STATUS_GRADED)
                .set(KyzzExamQuestion::getGradingMethod, GRADING_METHOD_OBJECTIVE)
                .set(KyzzExamQuestion::getReferenceAnswer, String.join(",", correctKeys))
                .set(KyzzExamQuestion::getAnalysisSnapshot, trimToNull(question.getAnalysis()))
                .set(KyzzExamQuestion::getGradingComment, correct ? "选择题答案正确。" : "选择题答案错误或未作答。")
                .set(KyzzExamQuestion::getGradingConfidence, null)
                .set(KyzzExamQuestion::getGradingPointsJson, null)
                .set(KyzzExamQuestion::getLlmRecordId, null)
                .set(KyzzExamQuestion::getGradedAt, gradedAt));
        return new QuestionGrade(true, awardedScore);
    }

    private QuestionGrade gradeShortQuestion(KyzzExamQuestion examQuestion, KyzzQuestion question) {
        String submittedAnswer = trimToNull(examQuestion.getAnswerContent());
        String referenceAnswer = trimToNull(question.getAnswerText());
        String analysis = trimToNull(question.getAnalysis());
        if (!StringUtils.hasText(submittedAnswer)) {
            BigDecimal zero = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            updateShortQuestionGrade(
                    examQuestion,
                    zero,
                    GRADING_METHOD_BLANK,
                    referenceAnswer,
                    analysis,
                    "未作答，简答题 0 分。",
                    null,
                    List.of(),
                    List.of("未作答"),
                    null
            );
            return new QuestionGrade(false, zero);
        }
        if (!StringUtils.hasText(referenceAnswer) && !StringUtils.hasText(analysis)) {
            markQuestionFailed(examQuestion, question, "简答题缺少标准答案或解析，无法进行 AI 阅卷");
            throw new IllegalStateException("简答题缺少标准答案或解析，无法进行 AI 阅卷");
        }

        LlmGenerateResult result = llmService.generateJson(
                LLM_SCENE,
                buildShortGradingSystemPrompt(),
                buildShortGradingUserPrompt(examQuestion, question, submittedAnswer, buildRagReferences(question)),
                "kyzz_short_answer_grading",
                SHORT_GRADING_SCHEMA
        );
        ShortGradePayload payload = parseShortGradePayload(result.getJsonContent(), examQuestion.getScore());
        updateShortQuestionGrade(
                examQuestion,
                payload.awardedScore(),
                GRADING_METHOD_LLM,
                referenceAnswer,
                analysis,
                payload.comment(),
                payload.confidence(),
                payload.matchedPoints(),
                payload.missingPoints(),
                result.getRecordId()
        );
        return new QuestionGrade(false, payload.awardedScore());
    }

    private void updateShortQuestionGrade(KyzzExamQuestion examQuestion,
                                          BigDecimal awardedScore,
                                          String method,
                                          String referenceAnswer,
                                          String analysis,
                                          String comment,
                                          BigDecimal confidence,
                                          List<String> matchedPoints,
                                          List<String> missingPoints,
                                          Long llmRecordId) {
        LocalDateTime gradedAt = LocalDateTime.now();
        kyzzExamQuestionMapper.update(null, new LambdaUpdateWrapper<KyzzExamQuestion>()
                .eq(KyzzExamQuestion::getId, examQuestion.getId())
                .set(KyzzExamQuestion::getAwardedScore, awardedScore)
                .set(KyzzExamQuestion::getIsCorrect, null)
                .set(KyzzExamQuestion::getGradingStatus, GRADING_STATUS_GRADED)
                .set(KyzzExamQuestion::getGradingMethod, method)
                .set(KyzzExamQuestion::getReferenceAnswer, referenceAnswer)
                .set(KyzzExamQuestion::getAnalysisSnapshot, analysis)
                .set(KyzzExamQuestion::getGradingComment, trimToLimit(comment, 1000))
                .set(KyzzExamQuestion::getGradingConfidence, confidence)
                .set(KyzzExamQuestion::getGradingPointsJson, toPointsJson(matchedPoints, missingPoints))
                .set(KyzzExamQuestion::getLlmRecordId, llmRecordId)
                .set(KyzzExamQuestion::getGradedAt, gradedAt));
    }

    private void markQuestionFailed(KyzzExamQuestion examQuestion, KyzzQuestion question, String message) {
        kyzzExamQuestionMapper.update(null, new LambdaUpdateWrapper<KyzzExamQuestion>()
                .eq(KyzzExamQuestion::getId, examQuestion.getId())
                .set(KyzzExamQuestion::getAwardedScore, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .set(KyzzExamQuestion::getGradingStatus, GRADING_STATUS_FAILED)
                .set(KyzzExamQuestion::getGradingComment, trimToLimit(message, 1000))
                .set(KyzzExamQuestion::getReferenceAnswer, question == null ? null : trimToNull(question.getAnswerText()))
                .set(KyzzExamQuestion::getAnalysisSnapshot, question == null ? null : trimToNull(question.getAnalysis()))
                .set(KyzzExamQuestion::getGradedAt, LocalDateTime.now()));
    }

    private void markSessionFailed(Long sessionId, String message) {
        kyzzExamSessionMapper.update(null, new LambdaUpdateWrapper<KyzzExamSession>()
                .eq(KyzzExamSession::getId, sessionId)
                .eq(KyzzExamSession::getGradingStatus, GRADING_STATUS_GRADING)
                .set(KyzzExamSession::getGradingStatus, GRADING_STATUS_FAILED)
                .set(KyzzExamSession::getGradingErrorMessage, trimToLimit(
                        StringUtils.hasText(message) ? message : "阅卷失败，请稍后重试",
                        500
                ))
                .set(KyzzExamSession::getGradedAt, LocalDateTime.now()));
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

    private List<ReferenceQuestion> buildRagReferences(KyzzQuestion target) {
        Set<Long> targetTagIds = loadTagIds(target.getId());
        Map<Long, KyzzQuestion> candidateMap = new LinkedHashMap<>();
        loadSameBankOrCategoryCandidates(target).forEach(question -> candidateMap.put(question.getId(), question));
        if (!targetTagIds.isEmpty()) {
            List<KyzzQuestionTagRel> rels = kyzzQuestionTagRelMapper.selectList(new LambdaQueryWrapper<KyzzQuestionTagRel>()
                    .in(KyzzQuestionTagRel::getTagId, targetTagIds)
                    .ne(KyzzQuestionTagRel::getQuestionId, target.getId()));
            List<Long> taggedQuestionIds = rels.stream()
                    .map(KyzzQuestionTagRel::getQuestionId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            if (!taggedQuestionIds.isEmpty()) {
                kyzzQuestionMapper.selectList(new LambdaQueryWrapper<KyzzQuestion>()
                                .in(KyzzQuestion::getId, taggedQuestionIds)
                                .eq(KyzzQuestion::getQuestionType, QUESTION_TYPE_SHORT)
                                .eq(KyzzQuestion::getStatus, 1)
                                .last("limit 40"))
                        .forEach(question -> candidateMap.putIfAbsent(question.getId(), question));
            }
        }
        if (candidateMap.isEmpty()) {
            return List.of();
        }

        Map<Long, Set<Long>> candidateTagMap = loadTagMap(candidateMap.keySet());
        return candidateMap.values().stream()
                .filter(question -> StringUtils.hasText(question.getAnswerText()) || StringUtils.hasText(question.getAnalysis()))
                .sorted(Comparator
                        .comparingInt((KyzzQuestion question) -> ragScore(target, targetTagIds, candidateTagMap.getOrDefault(question.getId(), Set.of()), question))
                        .reversed()
                        .thenComparing(KyzzQuestion::getSortNo, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(KyzzQuestion::getId))
                .limit(MAX_REFERENCE_COUNT)
                .map(question -> new ReferenceQuestion(question.getStem(), question.getAnswerText(), question.getAnalysis()))
                .toList();
    }

    private List<KyzzQuestion> loadSameBankOrCategoryCandidates(KyzzQuestion target) {
        LambdaQueryWrapper<KyzzQuestion> wrapper = new LambdaQueryWrapper<KyzzQuestion>()
                .eq(KyzzQuestion::getQuestionType, QUESTION_TYPE_SHORT)
                .eq(KyzzQuestion::getStatus, 1)
                .ne(KyzzQuestion::getId, target.getId())
                .orderByAsc(KyzzQuestion::getSortNo)
                .orderByAsc(KyzzQuestion::getId)
                .last("limit 40");
        if (target.getCategoryId() != null) {
            wrapper.and(item -> item.eq(KyzzQuestion::getQuestionBankId, target.getQuestionBankId())
                    .or()
                    .eq(KyzzQuestion::getCategoryId, target.getCategoryId()));
        } else {
            wrapper.eq(KyzzQuestion::getQuestionBankId, target.getQuestionBankId());
        }
        return kyzzQuestionMapper.selectList(wrapper);
    }

    private Set<Long> loadTagIds(Long questionId) {
        if (questionId == null) {
            return Set.of();
        }
        return kyzzQuestionTagRelMapper.selectList(new LambdaQueryWrapper<KyzzQuestionTagRel>()
                        .eq(KyzzQuestionTagRel::getQuestionId, questionId))
                .stream()
                .map(KyzzQuestionTagRel::getTagId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Map<Long, Set<Long>> loadTagMap(Collection<Long> questionIds) {
        LinkedHashSet<Long> ids = normalizeIds(questionIds);
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, Set<Long>> result = new LinkedHashMap<>();
        kyzzQuestionTagRelMapper.selectList(new LambdaQueryWrapper<KyzzQuestionTagRel>()
                        .in(KyzzQuestionTagRel::getQuestionId, ids))
                .forEach(rel -> result.computeIfAbsent(rel.getQuestionId(), key -> new LinkedHashSet<>()).add(rel.getTagId()));
        return result;
    }

    private int ragScore(KyzzQuestion target, Set<Long> targetTagIds, Set<Long> candidateTagIds, KyzzQuestion candidate) {
        Set<Long> overlap = new HashSet<>(candidateTagIds);
        overlap.retainAll(targetTagIds);
        int score = overlap.size() * 10;
        if (Objects.equals(target.getCategoryId(), candidate.getCategoryId())) {
            score += 4;
        }
        if (Objects.equals(target.getQuestionBankId(), candidate.getQuestionBankId())) {
            score += 3;
        }
        score += Math.min(5, textOverlapScore(target.getStem(), candidate.getStem()));
        return score;
    }

    private int textOverlapScore(String left, String right) {
        Set<String> leftTerms = buildTextTerms(left);
        Set<String> rightTerms = buildTextTerms(right);
        if (leftTerms.isEmpty() || rightTerms.isEmpty()) {
            return 0;
        }
        leftTerms.retainAll(rightTerms);
        return leftTerms.size();
    }

    private Set<String> buildTextTerms(String text) {
        if (!StringUtils.hasText(text)) {
            return Set.of();
        }
        String normalized = text.toLowerCase(Locale.ROOT).replaceAll("[\\s\\p{Punct}，。；：！？、（）【】《》“”‘’]", "");
        if (normalized.length() < 2) {
            return Set.of(normalized);
        }
        Set<String> result = new LinkedHashSet<>();
        int max = Math.min(normalized.length() - 1, 80);
        for (int index = 0; index < max; index++) {
            result.add(normalized.substring(index, index + 2));
        }
        return result;
    }

    private String buildShortGradingSystemPrompt() {
        return "你是考研政治简答题阅卷老师。请严格依据题干、标准答案、解析和相似题参考资料评分。"
                + "只评价学生答案，不补充题外知识；分数必须在 0 到本题满分之间；输出必须符合 JSON Schema。";
    }

    private String buildShortGradingUserPrompt(KyzzExamQuestion examQuestion,
                                               KyzzQuestion question,
                                               String submittedAnswer,
                                               List<ReferenceQuestion> references) {
        StringBuilder builder = new StringBuilder();
        builder.append("题干：\n").append(trimToLimit(question.getStem(), 1200)).append("\n\n");
        builder.append("本题满分：").append(normalizeMoney(examQuestion.getScore())).append(" 分\n\n");
        builder.append("学生答案：\n").append(trimToLimit(submittedAnswer, 3000)).append("\n\n");
        builder.append("本题标准答案：\n").append(defaultText(question.getAnswerText(), "无")).append("\n\n");
        builder.append("本题解析：\n").append(defaultText(question.getAnalysis(), "无")).append("\n\n");
        if (!references.isEmpty()) {
            builder.append("相似题参考资料：\n");
            for (int index = 0; index < references.size(); index++) {
                ReferenceQuestion reference = references.get(index);
                builder.append(index + 1).append(". 题干：").append(trimToLimit(reference.stem(), 400)).append("\n");
                builder.append("参考答案：").append(defaultText(reference.answerText(), "无")).append("\n");
                builder.append("解析：").append(defaultText(reference.analysis(), "无")).append("\n");
            }
            builder.append("\n");
        }
        builder.append("请给出 awardedScore、confidence、comment、matchedPoints、missingPoints。");
        return builder.toString();
    }

    private ShortGradePayload parseShortGradePayload(String jsonContent, BigDecimal maxScore) {
        try {
            JsonNode root = objectMapper.readTree(jsonContent);
            BigDecimal awardedScore = clampScore(root.path("awardedScore").decimalValue(), maxScore);
            BigDecimal confidence = clampConfidence(root.path("confidence").decimalValue());
            String comment = trimToLimit(root.path("comment").asText(""), 1000);
            List<String> matchedPoints = readStringList(root.path("matchedPoints"));
            List<String> missingPoints = readStringList(root.path("missingPoints"));
            return new ShortGradePayload(awardedScore, confidence, comment, matchedPoints, missingPoints);
        } catch (Exception ex) {
            throw new IllegalStateException("AI 阅卷结果解析失败：" + ex.getMessage(), ex);
        }
    }

    private List<String> readStringList(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (JsonNode item : node) {
            String value = trimToLimit(item.asText(""), 200);
            if (StringUtils.hasText(value)) {
                result.add(value);
            }
        }
        return result;
    }

    private String toPointsJson(List<String> matchedPoints, List<String> missingPoints) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "matchedPoints", matchedPoints == null ? List.of() : matchedPoints,
                    "missingPoints", missingPoints == null ? List.of() : missingPoints
            ));
        } catch (Exception ex) {
            return null;
        }
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

    private List<String> parseStoredOptionKeys(String answerContent) {
        if (!StringUtils.hasText(answerContent)) {
            return List.of();
        }
        return List.of(answerContent.split(",")).stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(item -> item.toUpperCase(Locale.ROOT))
                .sorted()
                .toList();
    }

    private boolean isChoiceQuestion(String questionType) {
        return QUESTION_TYPE_SINGLE.equals(questionType) || QUESTION_TYPE_MULTIPLE.equals(questionType);
    }

    private BigDecimal clampScore(BigDecimal value, BigDecimal maxScore) {
        BigDecimal normalizedMax = normalizeMoney(maxScore);
        BigDecimal normalized = value == null ? BigDecimal.ZERO : normalizeMoney(value);
        if (normalized.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (normalized.compareTo(normalizedMax) > 0) {
            return normalizedMax;
        }
        return normalized;
    }

    private BigDecimal clampConfidence(BigDecimal value) {
        BigDecimal normalized = value == null ? BigDecimal.ZERO : value.setScale(4, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        if (normalized.compareTo(BigDecimal.ONE) > 0) {
            return BigDecimal.ONE.setScale(4, RoundingMode.HALF_UP);
        }
        return normalized;
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String defaultText(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimToLimit(trimmed, 1200);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String trimToLimit(String value, int limit) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= limit) {
            return trimmed;
        }
        return trimmed.substring(0, limit);
    }

    private LinkedHashSet<Long> normalizeIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private record QuestionGrade(boolean objective, BigDecimal awardedScore) {
    }

    private record ShortGradePayload(BigDecimal awardedScore,
                                     BigDecimal confidence,
                                     String comment,
                                     List<String> matchedPoints,
                                     List<String> missingPoints) {
    }

    private record ReferenceQuestion(String stem, String answerText, String analysis) {
    }
}
