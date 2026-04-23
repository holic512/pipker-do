package org.example.backend.biz.kyzz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.biz.kyzz.dto.KyzzQuestionBankMineRecordResponse;
import org.example.backend.biz.kyzz.dto.KyzzQuestionBankMineResponse;
import org.example.backend.biz.kyzz.dto.KyzzQuestionBankMineSummaryResponse;
import org.example.backend.biz.kyzz.dto.KyzzQuestionBankPublicCategoryResponse;
import org.example.backend.biz.kyzz.dto.KyzzQuestionBankPublicRecordResponse;
import org.example.backend.biz.kyzz.dto.KyzzQuestionBankPublicResponse;
import org.example.backend.biz.kyzz.dto.KyzzQuestionBankPublicSummaryResponse;
import org.example.backend.biz.kyzz.dto.KyzzQuestionBankSelectionRequest;
import org.example.backend.biz.kyzz.entity.KyzzCategory;
import org.example.backend.biz.kyzz.entity.KyzzQuestionBank;
import org.example.backend.biz.kyzz.entity.KyzzUserAnswer;
import org.example.backend.biz.kyzz.entity.KyzzUserQuestionBank;
import org.example.backend.biz.kyzz.mapper.KyzzCategoryMapper;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionBankMapper;
import org.example.backend.biz.kyzz.mapper.KyzzUserAnswerMapper;
import org.example.backend.biz.kyzz.mapper.KyzzUserQuestionBankMapper;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.storage.service.LocalFileStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI 索引: KYZZ 用户侧题库服务。
 */
@Service
public class KyzzQuestionBankUserService {

    private static final BigDecimal ZERO_PROGRESS = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal FULL_PROGRESS = BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);
    private static final Set<String> SUPPORTED_SELECTION_STATUS = Set.of("all", "selected", "unselected");

    private final KyzzQuestionBankMapper kyzzQuestionBankMapper;
    private final KyzzUserQuestionBankMapper kyzzUserQuestionBankMapper;
    private final KyzzCategoryMapper kyzzCategoryMapper;
    private final KyzzUserAnswerMapper kyzzUserAnswerMapper;
    private final LocalFileStorage localFileStorage;

    public KyzzQuestionBankUserService(KyzzQuestionBankMapper kyzzQuestionBankMapper,
                                       KyzzUserQuestionBankMapper kyzzUserQuestionBankMapper,
                                       KyzzCategoryMapper kyzzCategoryMapper,
                                       KyzzUserAnswerMapper kyzzUserAnswerMapper,
                                       LocalFileStorage localFileStorage) {
        this.kyzzQuestionBankMapper = kyzzQuestionBankMapper;
        this.kyzzUserQuestionBankMapper = kyzzUserQuestionBankMapper;
        this.kyzzCategoryMapper = kyzzCategoryMapper;
        this.kyzzUserAnswerMapper = kyzzUserAnswerMapper;
        this.localFileStorage = localFileStorage;
    }

    public KyzzQuestionBankMineResponse getMineQuestionBanks(Long userId) {
        List<KyzzUserQuestionBank> relations = kyzzUserQuestionBankMapper.selectList(new LambdaQueryWrapper<KyzzUserQuestionBank>()
                .eq(KyzzUserQuestionBank::getUserId, userId)
                .orderByDesc(KyzzUserQuestionBank::getCreatedAt)
                .orderByDesc(KyzzUserQuestionBank::getId));
        if (relations.isEmpty()) {
            return new KyzzQuestionBankMineResponse(new KyzzQuestionBankMineSummaryResponse(0, 0, 0), List.of());
        }

        Map<Long, KyzzUserQuestionBank> relationMap = relations.stream()
                .collect(Collectors.toMap(KyzzUserQuestionBank::getQuestionBankId, item -> item, (left, right) -> left, LinkedHashMap::new));
        List<KyzzQuestionBank> activeBanks = kyzzQuestionBankMapper.selectList(new LambdaQueryWrapper<KyzzQuestionBank>()
                .in(KyzzQuestionBank::getId, relationMap.keySet())
                .eq(KyzzQuestionBank::getStatus, 1));
        if (activeBanks.isEmpty()) {
            return new KyzzQuestionBankMineResponse(new KyzzQuestionBankMineSummaryResponse(0, 0, 0), List.of());
        }

        Map<Long, KyzzCategory> categoryMap = buildCategoryMap();
        Map<Long, QuestionBankProgressSnapshot> progressMap = buildProgressSnapshotMap(userId, toBankIds(activeBanks), activeBanks);
        List<KyzzQuestionBankMineRecordResponse> records = activeBanks.stream()
                .map(bank -> toMineRecord(bank, relationMap.get(bank.getId()), categoryMap.get(bank.getCategoryId()), progressMap.get(bank.getId())))
                .sorted(Comparator
                        .comparing(KyzzQuestionBankMineRecordResponse::getLastPracticeAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(KyzzQuestionBankMineRecordResponse::getJoinedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        int selectedCount = records.size();
        int completedCount = (int) records.stream()
                .filter(item -> item.getQuestionCount() != null && item.getQuestionCount() > 0)
                .filter(item -> item.getCurrentProgress() != null && item.getCurrentProgress().compareTo(FULL_PROGRESS) >= 0)
                .count();
        int inProgressCount = (int) records.stream()
                .filter(item -> item.getCurrentProgress() != null)
                .filter(item -> item.getCurrentProgress().compareTo(ZERO_PROGRESS) > 0)
                .filter(item -> item.getCurrentProgress().compareTo(FULL_PROGRESS) < 0)
                .count();
        return new KyzzQuestionBankMineResponse(
                new KyzzQuestionBankMineSummaryResponse(selectedCount, inProgressCount, completedCount),
                records
        );
    }

    public KyzzQuestionBankPublicResponse getPublicQuestionBanks(Long userId,
                                                                 String keyword,
                                                                 Long categoryId,
                                                                 Integer difficultyLevel,
                                                                 String selectionStatus) {
        String normalizedSelectionStatus = normalizeSelectionStatus(selectionStatus);
        validateDifficultyLevel(difficultyLevel);

        List<KyzzQuestionBank> activeBanks = kyzzQuestionBankMapper.selectList(new LambdaQueryWrapper<KyzzQuestionBank>()
                .eq(KyzzQuestionBank::getStatus, 1)
                .orderByAsc(KyzzQuestionBank::getSortNo)
                .orderByDesc(KyzzQuestionBank::getId));
        Map<Long, KyzzCategory> categoryMap = buildCategoryMap();
        Map<Long, KyzzUserQuestionBank> selectedRelationMap = buildSelectedRelationMap(userId, toBankIds(activeBanks));
        Map<Long, QuestionBankProgressSnapshot> progressMap = buildProgressSnapshotMap(userId, selectedRelationMap.keySet(), activeBanks);

        List<KyzzQuestionBank> sortedActiveBanks = activeBanks.stream()
                .sorted(Comparator
                        .comparing(KyzzQuestionBank::getSortNo, Comparator.nullsFirst(Integer::compareTo))
                        .thenComparing(KyzzQuestionBank::getStudyUserCount, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(KyzzQuestionBank::getId, Comparator.reverseOrder()))
                .toList();
        List<KyzzQuestionBankPublicCategoryResponse> categories = buildPublicCategories(sortedActiveBanks, categoryMap);
        List<KyzzQuestionBankPublicRecordResponse> records = sortedActiveBanks.stream()
                .filter(bank -> matchesKeyword(bank, categoryMap.get(bank.getCategoryId()), keyword))
                .filter(bank -> categoryId == null || Objects.equals(bank.getCategoryId(), categoryId))
                .filter(bank -> difficultyLevel == null || Objects.equals(bank.getDifficultyLevel(), difficultyLevel))
                .map(bank -> {
                    KyzzUserQuestionBank relation = selectedRelationMap.get(bank.getId());
                    boolean selected = relation != null;
                    if ("selected".equals(normalizedSelectionStatus) && !selected) {
                        return null;
                    }
                    if ("unselected".equals(normalizedSelectionStatus) && selected) {
                        return null;
                    }
                    return toPublicRecord(bank, relation, categoryMap.get(bank.getCategoryId()), progressMap.get(bank.getId()), selected);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(KyzzQuestionBankPublicRecordResponse::getSelected, Comparator.reverseOrder()))
                .toList();

        int selectedCount = (int) records.stream().filter(item -> Boolean.TRUE.equals(item.getSelected())).count();
        int unselectedCount = records.size() - selectedCount;
        return new KyzzQuestionBankPublicResponse(
                new KyzzQuestionBankPublicSummaryResponse(records.size(), selectedCount, unselectedCount),
                categories,
                records
        );
    }

    @Transactional
    public KyzzQuestionBankPublicRecordResponse updateQuestionBankSelection(Long userId,
                                                                            Long bankId,
                                                                            KyzzQuestionBankSelectionRequest request) {
        if (request == null || request.getSelected() == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "请选择要执行的题库操作");
        }

        KyzzQuestionBank bank = requireActiveQuestionBank(bankId);
        KyzzUserQuestionBank relation = kyzzUserQuestionBankMapper.selectOne(new LambdaQueryWrapper<KyzzUserQuestionBank>()
                .eq(KyzzUserQuestionBank::getUserId, userId)
                .eq(KyzzUserQuestionBank::getQuestionBankId, bankId)
                .last("limit 1"));
        if (Boolean.TRUE.equals(request.getSelected())) {
            if (relation == null) {
                relation = new KyzzUserQuestionBank();
                relation.setUserId(userId);
                relation.setQuestionBankId(bankId);
                relation.setJoinSource("manual");
                relation.setCurrentProgress(ZERO_PROGRESS);
                relation.setStudiedCount(0);
                relation.setCorrectCount(0);
                relation.setWrongCount(0);
                kyzzUserQuestionBankMapper.insert(relation);
            }
        } else if (relation != null) {
            kyzzUserQuestionBankMapper.deleteById(relation.getId());
            relation = null;
        }

        syncStudyUserCount(bankId);
        KyzzQuestionBank refreshedBank = requireActiveQuestionBank(bankId);
        KyzzCategory category = refreshedBank.getCategoryId() == null ? null : kyzzCategoryMapper.selectById(refreshedBank.getCategoryId());

        if (relation != null) {
            Map<Long, QuestionBankProgressSnapshot> progressMap = buildProgressSnapshotMap(userId, List.of(bankId), List.of(refreshedBank));
            QuestionBankProgressSnapshot snapshot = progressMap.getOrDefault(bankId, QuestionBankProgressSnapshot.empty());
            syncRelationSnapshot(relation.getId(), snapshot);
            relation = kyzzUserQuestionBankMapper.selectById(relation.getId());
            return toPublicRecord(refreshedBank, relation, category, snapshot, true);
        }
        return toPublicRecord(refreshedBank, null, category, null, false);
    }

    private Map<Long, KyzzCategory> buildCategoryMap() {
        List<KyzzCategory> categories = kyzzCategoryMapper.selectList(new LambdaQueryWrapper<KyzzCategory>()
                .orderByAsc(KyzzCategory::getCategoryLevel)
                .orderByAsc(KyzzCategory::getSortNo)
                .orderByAsc(KyzzCategory::getId));
        Map<Long, KyzzCategory> result = new HashMap<>();
        categories.forEach(category -> result.put(category.getId(), category));
        return result;
    }

    private Map<Long, KyzzUserQuestionBank> buildSelectedRelationMap(Long userId, Collection<Long> bankIds) {
        if (bankIds == null || bankIds.isEmpty()) {
            return Map.of();
        }
        List<KyzzUserQuestionBank> relations = kyzzUserQuestionBankMapper.selectList(new LambdaQueryWrapper<KyzzUserQuestionBank>()
                .eq(KyzzUserQuestionBank::getUserId, userId)
                .in(KyzzUserQuestionBank::getQuestionBankId, bankIds)
                .orderByDesc(KyzzUserQuestionBank::getCreatedAt)
                .orderByDesc(KyzzUserQuestionBank::getId));
        Map<Long, KyzzUserQuestionBank> result = new LinkedHashMap<>();
        relations.forEach(item -> result.putIfAbsent(item.getQuestionBankId(), item));
        return result;
    }

    private List<KyzzQuestionBankPublicCategoryResponse> buildPublicCategories(List<KyzzQuestionBank> banks,
                                                                               Map<Long, KyzzCategory> categoryMap) {
        return banks.stream()
                .map(KyzzQuestionBank::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .map(categoryMap::get)
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing(KyzzCategory::getCategoryLevel, Comparator.nullsFirst(Integer::compareTo))
                        .thenComparing(KyzzCategory::getSortNo, Comparator.nullsFirst(Integer::compareTo))
                        .thenComparing(KyzzCategory::getId))
                .map(category -> new KyzzQuestionBankPublicCategoryResponse(
                        category.getId(),
                        category.getCategoryName(),
                        category.getCategoryLevel()
                ))
                .toList();
    }

    private Map<Long, QuestionBankProgressSnapshot> buildProgressSnapshotMap(Long userId,
                                                                             Collection<Long> bankIds,
                                                                             List<KyzzQuestionBank> banks) {
        LinkedHashSet<Long> normalizedBankIds = bankIds == null
                ? new LinkedHashSet<>()
                : bankIds.stream().filter(Objects::nonNull).collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
        if (normalizedBankIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Integer> questionCountByBankId = new HashMap<>();
        banks.forEach(bank -> questionCountByBankId.put(bank.getId(), bank.getQuestionCount() == null ? 0 : bank.getQuestionCount()));

        List<KyzzUserAnswer> answers = kyzzUserAnswerMapper.selectList(new LambdaQueryWrapper<KyzzUserAnswer>()
                .eq(KyzzUserAnswer::getUserId, userId)
                .in(KyzzUserAnswer::getQuestionBankId, normalizedBankIds)
                .orderByAsc(KyzzUserAnswer::getQuestionBankId)
                .orderByAsc(KyzzUserAnswer::getQuestionId)
                .orderByDesc(KyzzUserAnswer::getSubmittedAt)
                .orderByDesc(KyzzUserAnswer::getId));

        Map<Long, Map<Long, KyzzUserAnswer>> latestAnswerByBankQuestion = new HashMap<>();
        for (KyzzUserAnswer answer : answers) {
            Map<Long, KyzzUserAnswer> bankAnswerMap = latestAnswerByBankQuestion.computeIfAbsent(answer.getQuestionBankId(), key -> new LinkedHashMap<>());
            bankAnswerMap.putIfAbsent(answer.getQuestionId(), answer);
        }

        Map<Long, QuestionBankProgressSnapshot> result = new HashMap<>();
        normalizedBankIds.forEach(bankId -> {
            Map<Long, KyzzUserAnswer> latestAnswers = latestAnswerByBankQuestion.getOrDefault(bankId, Map.of());
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

    private BigDecimal buildProgress(int studiedCount, int questionCount) {
        if (studiedCount <= 0 || questionCount <= 0) {
            return ZERO_PROGRESS;
        }
        BigDecimal progress = BigDecimal.valueOf(studiedCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(questionCount), 2, RoundingMode.HALF_UP);
        return progress.compareTo(FULL_PROGRESS) > 0 ? FULL_PROGRESS : progress;
    }

    private KyzzQuestionBankMineRecordResponse toMineRecord(KyzzQuestionBank bank,
                                                            KyzzUserQuestionBank relation,
                                                            KyzzCategory category,
                                                            QuestionBankProgressSnapshot snapshot) {
        QuestionBankProgressSnapshot resolvedSnapshot = snapshot == null ? QuestionBankProgressSnapshot.empty() : snapshot;
        return new KyzzQuestionBankMineRecordResponse(
                bank.getId(),
                bank.getBankCode(),
                bank.getBankName(),
                bank.getSubtitle(),
                resolveCoverUrl(bank.getCoverUrl()),
                bank.getCategoryId(),
                category == null ? null : category.getCategoryName(),
                bank.getDifficultyLevel(),
                bank.getQuestionCount() == null ? 0 : bank.getQuestionCount(),
                normalizeScore(bank.getTotalScore()),
                bank.getRatingCount() == null ? 0 : bank.getRatingCount(),
                bank.getStudyUserCount() == null ? 0 : bank.getStudyUserCount(),
                resolvedSnapshot.currentProgress(),
                resolvedSnapshot.studiedCount(),
                resolvedSnapshot.correctCount(),
                resolvedSnapshot.wrongCount(),
                resolvedSnapshot.lastPracticeAt(),
                relation == null ? null : relation.getJoinSource(),
                relation == null ? null : relation.getCreatedAt()
        );
    }

    private KyzzQuestionBankPublicRecordResponse toPublicRecord(KyzzQuestionBank bank,
                                                                KyzzUserQuestionBank relation,
                                                                KyzzCategory category,
                                                                QuestionBankProgressSnapshot snapshot,
                                                                boolean selected) {
        QuestionBankProgressSnapshot resolvedSnapshot = selected
                ? (snapshot == null ? QuestionBankProgressSnapshot.empty() : snapshot)
                : null;
        return new KyzzQuestionBankPublicRecordResponse(
                bank.getId(),
                bank.getBankCode(),
                bank.getBankName(),
                bank.getSubtitle(),
                resolveCoverUrl(bank.getCoverUrl()),
                bank.getCategoryId(),
                category == null ? null : category.getCategoryName(),
                bank.getDifficultyLevel(),
                bank.getQuestionCount() == null ? 0 : bank.getQuestionCount(),
                normalizeScore(bank.getTotalScore()),
                bank.getRatingCount() == null ? 0 : bank.getRatingCount(),
                bank.getStudyUserCount() == null ? 0 : bank.getStudyUserCount(),
                bank.getSortNo() == null ? 0 : bank.getSortNo(),
                resolvedSnapshot == null ? null : resolvedSnapshot.currentProgress(),
                resolvedSnapshot == null ? null : resolvedSnapshot.studiedCount(),
                resolvedSnapshot == null ? null : resolvedSnapshot.correctCount(),
                resolvedSnapshot == null ? null : resolvedSnapshot.wrongCount(),
                resolvedSnapshot == null ? null : resolvedSnapshot.lastPracticeAt(),
                selected && relation != null ? relation.getJoinSource() : null,
                selected && relation != null ? relation.getCreatedAt() : null,
                selected
        );
    }

    private String resolveCoverUrl(String coverValue) {
        if (!StringUtils.hasText(coverValue)) {
            return null;
        }
        if (localFileStorage.isManagedKey(coverValue)) {
            return localFileStorage.resolveUrl(coverValue);
        }
        return coverValue;
    }

    private BigDecimal normalizeScore(BigDecimal totalScore) {
        return totalScore == null ? ZERO_PROGRESS : totalScore.setScale(2, RoundingMode.HALF_UP);
    }

    private void validateDifficultyLevel(Integer difficultyLevel) {
        if (difficultyLevel == null) {
            return;
        }
        if (difficultyLevel < 1 || difficultyLevel > 4) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "难度等级仅支持 1 到 4");
        }
    }

    private String normalizeSelectionStatus(String selectionStatus) {
        if (!StringUtils.hasText(selectionStatus)) {
            return "all";
        }
        String normalized = selectionStatus.trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_SELECTION_STATUS.contains(normalized)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "selectionStatus 仅支持 all、selected、unselected");
        }
        return normalized;
    }

    private boolean matchesKeyword(KyzzQuestionBank bank, KyzzCategory category, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
        return contains(bank.getBankCode(), normalizedKeyword)
                || contains(bank.getBankName(), normalizedKeyword)
                || contains(bank.getSubtitle(), normalizedKeyword)
                || contains(category == null ? null : category.getCategoryName(), normalizedKeyword);
    }

    private boolean contains(String source, String keyword) {
        return StringUtils.hasText(source) && source.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private KyzzQuestionBank requireActiveQuestionBank(Long bankId) {
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

    private void syncStudyUserCount(Long bankId) {
        Long count = kyzzUserQuestionBankMapper.selectCount(new LambdaQueryWrapper<KyzzUserQuestionBank>()
                .eq(KyzzUserQuestionBank::getQuestionBankId, bankId));
        kyzzQuestionBankMapper.update(null, new LambdaUpdateWrapper<KyzzQuestionBank>()
                .eq(KyzzQuestionBank::getId, bankId)
                .set(KyzzQuestionBank::getStudyUserCount, count == null ? 0 : count.intValue()));
    }

    private void syncRelationSnapshot(Long relationId, QuestionBankProgressSnapshot snapshot) {
        kyzzUserQuestionBankMapper.update(null, new LambdaUpdateWrapper<KyzzUserQuestionBank>()
                .eq(KyzzUserQuestionBank::getId, relationId)
                .set(KyzzUserQuestionBank::getCurrentProgress, snapshot.currentProgress())
                .set(KyzzUserQuestionBank::getStudiedCount, snapshot.studiedCount())
                .set(KyzzUserQuestionBank::getCorrectCount, snapshot.correctCount())
                .set(KyzzUserQuestionBank::getWrongCount, snapshot.wrongCount())
                .set(KyzzUserQuestionBank::getLastPracticeAt, snapshot.lastPracticeAt()));
    }

    private List<Long> toBankIds(Collection<KyzzQuestionBank> banks) {
        if (banks == null || banks.isEmpty()) {
            return List.of();
        }
        List<Long> result = new ArrayList<>(banks.size());
        banks.forEach(bank -> result.add(bank.getId()));
        return result;
    }

    private record QuestionBankProgressSnapshot(BigDecimal currentProgress,
                                                Integer studiedCount,
                                                Integer correctCount,
                                                Integer wrongCount,
                                                LocalDateTime lastPracticeAt) {

        private static QuestionBankProgressSnapshot empty() {
            return new QuestionBankProgressSnapshot(ZERO_PROGRESS, 0, 0, 0, null);
        }
    }
}
