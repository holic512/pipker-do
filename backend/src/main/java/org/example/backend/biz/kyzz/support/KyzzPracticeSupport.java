package org.example.backend.biz.kyzz.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.biz.kyzz.entity.KyzzCategory;
import org.example.backend.biz.kyzz.entity.KyzzQuestion;
import org.example.backend.biz.kyzz.entity.KyzzQuestionBank;
import org.example.backend.biz.kyzz.entity.KyzzQuestionOption;
import org.example.backend.biz.kyzz.entity.KyzzUserAnswer;
import org.example.backend.biz.kyzz.entity.KyzzUserQuestionBank;
import org.example.backend.biz.kyzz.mapper.KyzzCategoryMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionBankMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionOptionMapper;
import org.example.backend.biz.kyzz.mapper.KyzzUserAnswerMapper;
import org.example.backend.biz.kyzz.mapper.KyzzUserQuestionBankMapper;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.storage.service.LocalFileStorage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AI 索引: KYZZ 用户侧刷题与题库快照支持。
 */
@Component
public class KyzzPracticeSupport {

    private static final String PRACTICE_SOURCE_TYPE_BANK = "bank";

    public static final String RESUME_STATUS_NOT_STARTED = "not_started";
    public static final String RESUME_STATUS_IN_PROGRESS = "in_progress";
    public static final String RESUME_STATUS_COMPLETED = "completed";
    public static final BigDecimal ZERO_PROGRESS = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    public static final BigDecimal FULL_PROGRESS = BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);

    private final KyzzCategoryMapper kyzzCategoryMapper;
    private final KyzzQuestionBankMapper kyzzQuestionBankMapper;
    private final KyzzQuestionMapper kyzzQuestionMapper;
    private final KyzzQuestionOptionMapper kyzzQuestionOptionMapper;
    private final KyzzUserAnswerMapper kyzzUserAnswerMapper;
    private final KyzzUserQuestionBankMapper kyzzUserQuestionBankMapper;
    private final LocalFileStorage localFileStorage;
    private final KyzzCacheService kyzzCacheService;

    public KyzzPracticeSupport(KyzzCategoryMapper kyzzCategoryMapper,
                               KyzzQuestionBankMapper kyzzQuestionBankMapper,
                               KyzzQuestionMapper kyzzQuestionMapper,
                               KyzzQuestionOptionMapper kyzzQuestionOptionMapper,
                               KyzzUserAnswerMapper kyzzUserAnswerMapper,
                               KyzzUserQuestionBankMapper kyzzUserQuestionBankMapper,
                               LocalFileStorage localFileStorage,
                               KyzzCacheService kyzzCacheService) {
        this.kyzzCategoryMapper = kyzzCategoryMapper;
        this.kyzzQuestionBankMapper = kyzzQuestionBankMapper;
        this.kyzzQuestionMapper = kyzzQuestionMapper;
        this.kyzzQuestionOptionMapper = kyzzQuestionOptionMapper;
        this.kyzzUserAnswerMapper = kyzzUserAnswerMapper;
        this.kyzzUserQuestionBankMapper = kyzzUserQuestionBankMapper;
        this.localFileStorage = localFileStorage;
        this.kyzzCacheService = kyzzCacheService;
    }

    public Map<Long, KyzzCategory> buildCategoryMap() {
        List<KyzzCategory> categories = kyzzCacheService.getList(KyzzCacheService.CATEGORY_LIST_KEY);
        if (categories == null) {
            categories = kyzzCategoryMapper.selectList(new LambdaQueryWrapper<KyzzCategory>()
                .orderByAsc(KyzzCategory::getCategoryLevel)
                .orderByAsc(KyzzCategory::getSortNo)
                .orderByAsc(KyzzCategory::getId));
            kyzzCacheService.putList(KyzzCacheService.CATEGORY_LIST_KEY, categories, KyzzCacheService.PUBLIC_BASE_TTL);
        }
        Map<Long, KyzzCategory> result = new HashMap<>();
        categories.forEach(category -> result.put(category.getId(), category));
        return result;
    }

    public Map<Long, KyzzUserQuestionBank> buildSelectedRelationMap(Long userId, Collection<Long> bankIds) {
        LinkedHashSet<Long> normalizedBankIds = normalizeIds(bankIds);
        if (normalizedBankIds.isEmpty()) {
            return Map.of();
        }
        List<KyzzUserQuestionBank> relations = kyzzUserQuestionBankMapper.selectList(new LambdaQueryWrapper<KyzzUserQuestionBank>()
                .eq(KyzzUserQuestionBank::getUserId, userId)
                .in(KyzzUserQuestionBank::getQuestionBankId, normalizedBankIds)
                .orderByDesc(KyzzUserQuestionBank::getCreatedAt)
                .orderByDesc(KyzzUserQuestionBank::getId));
        Map<Long, KyzzUserQuestionBank> result = new LinkedHashMap<>();
        relations.forEach(item -> result.putIfAbsent(item.getQuestionBankId(), item));
        return result;
    }

    public Map<Long, List<KyzzQuestion>> buildActiveQuestionMap(Collection<Long> bankIds) {
        LinkedHashSet<Long> normalizedBankIds = normalizeIds(bankIds);
        if (normalizedBankIds.isEmpty()) {
            return Map.of();
        }
        List<KyzzQuestion> questions = new ArrayList<>();
        List<Long> missedBankIds = new ArrayList<>();
        for (Long bankId : normalizedBankIds) {
            List<KyzzQuestion> cachedQuestions = kyzzCacheService.getList(kyzzCacheService.bankQuestionsKey(bankId));
            if (cachedQuestions == null) {
                missedBankIds.add(bankId);
            } else {
                questions.addAll(cachedQuestions);
            }
        }
        if (!missedBankIds.isEmpty()) {
            List<KyzzQuestion> missedQuestions = kyzzQuestionMapper.selectList(new LambdaQueryWrapper<KyzzQuestion>()
                    .in(KyzzQuestion::getQuestionBankId, missedBankIds)
                    .eq(KyzzQuestion::getStatus, 1)
                    .orderByAsc(KyzzQuestion::getQuestionBankId)
                    .orderByAsc(KyzzQuestion::getSortNo)
                    .orderByAsc(KyzzQuestion::getId));
            Map<Long, List<KyzzQuestion>> missedQuestionMap = new LinkedHashMap<>();
            missedQuestions.forEach(question -> missedQuestionMap.computeIfAbsent(question.getQuestionBankId(), key -> new ArrayList<>()).add(question));
            missedBankIds.forEach(bankId -> kyzzCacheService.putList(
                    kyzzCacheService.bankQuestionsKey(bankId),
                    missedQuestionMap.getOrDefault(bankId, List.of()),
                    KyzzCacheService.PUBLIC_BASE_TTL
            ));
            questions.addAll(missedQuestions);
        }
        Map<Long, List<KyzzQuestion>> result = new LinkedHashMap<>();
        questions.forEach(question -> result.computeIfAbsent(question.getQuestionBankId(), key -> new ArrayList<>()).add(question));
        return result;
    }

    public Map<Long, List<KyzzQuestionOption>> buildQuestionOptionMap(Collection<Long> questionIds) {
        LinkedHashSet<Long> normalizedQuestionIds = normalizeIds(questionIds);
        if (normalizedQuestionIds.isEmpty()) {
            return Map.of();
        }
        List<KyzzQuestionOption> options = new ArrayList<>();
        List<Long> missedQuestionIds = new ArrayList<>();
        for (Long questionId : normalizedQuestionIds) {
            List<KyzzQuestionOption> cachedOptions = kyzzCacheService.getList(kyzzCacheService.questionOptionsKey(questionId));
            if (cachedOptions == null) {
                missedQuestionIds.add(questionId);
            } else {
                options.addAll(cachedOptions);
            }
        }
        if (!missedQuestionIds.isEmpty()) {
            List<KyzzQuestionOption> missedOptions = kyzzQuestionOptionMapper.selectList(new LambdaQueryWrapper<KyzzQuestionOption>()
                    .in(KyzzQuestionOption::getQuestionId, missedQuestionIds)
                    .orderByAsc(KyzzQuestionOption::getQuestionId)
                    .orderByAsc(KyzzQuestionOption::getSortNo)
                    .orderByAsc(KyzzQuestionOption::getId));
            Map<Long, List<KyzzQuestionOption>> missedOptionMap = new LinkedHashMap<>();
            missedOptions.forEach(option -> missedOptionMap.computeIfAbsent(option.getQuestionId(), key -> new ArrayList<>()).add(option));
            missedQuestionIds.forEach(questionId -> kyzzCacheService.putList(
                    kyzzCacheService.questionOptionsKey(questionId),
                    missedOptionMap.getOrDefault(questionId, List.of()),
                    KyzzCacheService.PUBLIC_BASE_TTL
            ));
            options.addAll(missedOptions);
        }
        Map<Long, List<KyzzQuestionOption>> result = new LinkedHashMap<>();
        options.forEach(option -> result.computeIfAbsent(option.getQuestionId(), key -> new ArrayList<>()).add(option));
        return result;
    }

    public Map<Long, Map<Long, KyzzUserAnswer>> buildLatestAnswerByBankQuestionMap(Long userId, Collection<Long> bankIds) {
        LinkedHashSet<Long> normalizedBankIds = normalizeIds(bankIds);
        if (normalizedBankIds.isEmpty()) {
            return Map.of();
        }

        List<KyzzUserAnswer> answers = kyzzUserAnswerMapper.selectList(new LambdaQueryWrapper<KyzzUserAnswer>()
                .eq(KyzzUserAnswer::getUserId, userId)
                .in(KyzzUserAnswer::getQuestionBankId, normalizedBankIds)
                .eq(KyzzUserAnswer::getPracticeSourceType, PRACTICE_SOURCE_TYPE_BANK)
                .orderByAsc(KyzzUserAnswer::getQuestionBankId)
                .orderByAsc(KyzzUserAnswer::getQuestionId)
                .orderByDesc(KyzzUserAnswer::getSubmittedAt)
                .orderByDesc(KyzzUserAnswer::getId));

        Map<Long, Map<Long, KyzzUserAnswer>> result = new LinkedHashMap<>();
        for (KyzzUserAnswer answer : answers) {
            Map<Long, KyzzUserAnswer> bankAnswerMap = result.computeIfAbsent(answer.getQuestionBankId(), key -> new LinkedHashMap<>());
            bankAnswerMap.putIfAbsent(answer.getQuestionId(), answer);
        }
        return result;
    }

    public Map<Long, QuestionBankProgressSnapshot> buildProgressSnapshotMap(Long userId,
                                                                            Collection<Long> bankIds,
                                                                            Collection<KyzzQuestionBank> banks) {
        return buildProgressSnapshotMap(
                bankIds,
                banks,
                buildLatestAnswerByBankQuestionMap(userId, bankIds)
        );
    }

    public Map<Long, QuestionBankProgressSnapshot> buildProgressSnapshotMap(Collection<Long> bankIds,
                                                                            Collection<KyzzQuestionBank> banks,
                                                                            Map<Long, Map<Long, KyzzUserAnswer>> latestAnswerMap) {
        LinkedHashSet<Long> normalizedBankIds = normalizeIds(bankIds);
        if (normalizedBankIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Integer> questionCountByBankId = new HashMap<>();
        if (banks != null) {
            banks.forEach(bank -> questionCountByBankId.put(bank.getId(), bank.getQuestionCount() == null ? 0 : bank.getQuestionCount()));
        }

        Map<Long, QuestionBankProgressSnapshot> result = new LinkedHashMap<>();
        normalizedBankIds.forEach(bankId -> {
            Map<Long, KyzzUserAnswer> latestAnswers = latestAnswerMap.getOrDefault(bankId, Map.of());
            int studiedCount = 0;
            int correctCount = 0;
            int wrongCount = 0;
            LocalDateTime lastPracticeAt = null;
            for (KyzzUserAnswer answer : latestAnswers.values()) {
                if (answer.getSubmittedAt() != null && (lastPracticeAt == null || answer.getSubmittedAt().isAfter(lastPracticeAt))) {
                    lastPracticeAt = answer.getSubmittedAt();
                }
                if (!Objects.equals(answer.getAnswerStatus(), 1)) {
                    continue;
                }
                studiedCount++;
                if (Objects.equals(answer.getIsCorrect(), 1)) {
                    correctCount++;
                } else {
                    wrongCount++;
                }
            }
            result.put(bankId, new QuestionBankProgressSnapshot(
                    buildProgress(studiedCount, questionCountByBankId.getOrDefault(bankId, 0)),
                    studiedCount,
                    correctCount,
                    wrongCount,
                    lastPracticeAt
            ));
        });
        return result;
    }

    public Map<Long, QuestionBankResumeSnapshot> buildResumeSnapshotMap(Long userId,
                                                                        Collection<KyzzQuestionBank> banks) {
        List<Long> bankIds = banks == null ? List.of() : banks.stream().map(KyzzQuestionBank::getId).toList();
        Map<Long, List<KyzzQuestion>> questionMap = buildActiveQuestionMap(bankIds);
        Map<Long, Map<Long, KyzzUserAnswer>> latestAnswerMap = buildLatestAnswerByBankQuestionMap(userId, bankIds);
        return buildResumeSnapshotMap(banks, questionMap, latestAnswerMap);
    }

    public Map<Long, QuestionBankResumeSnapshot> buildResumeSnapshotMap(Collection<KyzzQuestionBank> banks,
                                                                        Map<Long, List<KyzzQuestion>> questionMap,
                                                                        Map<Long, Map<Long, KyzzUserAnswer>> latestAnswerMap) {
        if (banks == null || banks.isEmpty()) {
            return Map.of();
        }
        Map<Long, QuestionBankResumeSnapshot> result = new LinkedHashMap<>();
        for (KyzzQuestionBank bank : banks) {
            List<KyzzQuestion> questions = questionMap.getOrDefault(bank.getId(), List.of());
            Map<Long, KyzzUserAnswer> answers = latestAnswerMap.getOrDefault(bank.getId(), Map.of());
            result.put(bank.getId(), buildResumeSnapshot(questions, answers));
        }
        return result;
    }

    public QuestionBankResumeSnapshot buildResumeSnapshot(List<KyzzQuestion> questions,
                                                          Map<Long, KyzzUserAnswer> latestAnswers) {
        if (questions == null || questions.isEmpty()) {
            return QuestionBankResumeSnapshot.empty();
        }

        int studiedCount = 0;
        LocalDateTime lastPracticeAt = null;
        for (KyzzUserAnswer answer : latestAnswers.values()) {
            if (answer.getSubmittedAt() != null && (lastPracticeAt == null || answer.getSubmittedAt().isAfter(lastPracticeAt))) {
                lastPracticeAt = answer.getSubmittedAt();
            }
            if (Objects.equals(answer.getAnswerStatus(), 1)) {
                studiedCount++;
            }
        }

        for (int index = 0; index < questions.size(); index++) {
            KyzzQuestion question = questions.get(index);
            KyzzUserAnswer answer = latestAnswers.get(question.getId());
            if (answer == null) {
                if (studiedCount <= 0 && lastPracticeAt == null) {
                    return new QuestionBankResumeSnapshot(
                            RESUME_STATUS_NOT_STARTED,
                            "待开始",
                            question.getId(),
                            index + 1,
                            false
                    );
                }
                return new QuestionBankResumeSnapshot(
                        RESUME_STATUS_IN_PROGRESS,
                        "继续第 " + (index + 1) + " 题",
                        question.getId(),
                        index + 1,
                        false
                );
            }
            if (!Objects.equals(answer.getAnswerStatus(), 1)) {
                return new QuestionBankResumeSnapshot(
                        RESUME_STATUS_IN_PROGRESS,
                        "继续第 " + (index + 1) + " 题",
                        question.getId(),
                        index + 1,
                        false
                );
            }
        }

        KyzzQuestion firstQuestion = questions.get(0);
        return new QuestionBankResumeSnapshot(
                RESUME_STATUS_COMPLETED,
                "已完成",
                firstQuestion.getId(),
                1,
                true
        );
    }

    public KyzzQuestionBank requireActiveQuestionBank(Long bankId) {
        if (bankId == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题库不能为空");
        }
        KyzzQuestionBank bank = kyzzQuestionBankMapper.selectById(bankId);
        if (bank == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "题库不存在");
        }
        if (!Objects.equals(bank.getStatus(), 1)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "题库已下架，暂时无法操作");
        }
        return bank;
    }

    public String resolveCoverUrl(String coverValue) {
        if (!StringUtils.hasText(coverValue)) {
            return null;
        }
        if (localFileStorage.isManagedKey(coverValue)) {
            return localFileStorage.resolveUrl(coverValue);
        }
        return coverValue;
    }

    public void syncStudyUserCount(Long bankId) {
        Long count = kyzzUserQuestionBankMapper.selectCount(new LambdaQueryWrapper<KyzzUserQuestionBank>()
                .eq(KyzzUserQuestionBank::getQuestionBankId, bankId));
        kyzzQuestionBankMapper.update(null, new LambdaUpdateWrapper<KyzzQuestionBank>()
                .eq(KyzzQuestionBank::getId, bankId)
                .set(KyzzQuestionBank::getStudyUserCount, count == null ? 0 : count.intValue()));
    }

    public void syncRelationSnapshot(Long relationId, QuestionBankProgressSnapshot snapshot) {
        kyzzUserQuestionBankMapper.update(null, new LambdaUpdateWrapper<KyzzUserQuestionBank>()
                .eq(KyzzUserQuestionBank::getId, relationId)
                .set(KyzzUserQuestionBank::getCurrentProgress, snapshot.currentProgress())
                .set(KyzzUserQuestionBank::getStudiedCount, snapshot.studiedCount())
                .set(KyzzUserQuestionBank::getCorrectCount, snapshot.correctCount())
                .set(KyzzUserQuestionBank::getWrongCount, snapshot.wrongCount())
                .set(KyzzUserQuestionBank::getLastPracticeAt, snapshot.lastPracticeAt()));
    }

    public BigDecimal normalizeScore(BigDecimal totalScore) {
        return totalScore == null ? ZERO_PROGRESS : totalScore.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal buildProgress(int studiedCount, int questionCount) {
        if (studiedCount <= 0 || questionCount <= 0) {
            return ZERO_PROGRESS;
        }
        BigDecimal progress = BigDecimal.valueOf(studiedCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(questionCount), 2, RoundingMode.HALF_UP);
        return progress.compareTo(FULL_PROGRESS) > 0 ? FULL_PROGRESS : progress;
    }

    private LinkedHashSet<Long> normalizeIds(Collection<Long> ids) {
        LinkedHashSet<Long> result = new LinkedHashSet<>();
        if (ids == null || ids.isEmpty()) {
            return result;
        }
        ids.stream()
                .filter(Objects::nonNull)
                .forEach(result::add);
        return result;
    }

    public record QuestionBankProgressSnapshot(BigDecimal currentProgress,
                                               Integer studiedCount,
                                               Integer correctCount,
                                               Integer wrongCount,
                                               LocalDateTime lastPracticeAt) {

        public static QuestionBankProgressSnapshot empty() {
            return new QuestionBankProgressSnapshot(ZERO_PROGRESS, 0, 0, 0, null);
        }
    }

    public record QuestionBankResumeSnapshot(String resumeStatus,
                                             String resumeLabel,
                                             Long resumeQuestionId,
                                             Integer resumeQuestionIndex,
                                             Boolean completed) {

        public static QuestionBankResumeSnapshot empty() {
            return new QuestionBankResumeSnapshot(RESUME_STATUS_NOT_STARTED, "待开始", null, 0, false);
        }
    }
}
