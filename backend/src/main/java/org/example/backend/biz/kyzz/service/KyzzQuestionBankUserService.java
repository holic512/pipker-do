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
import org.example.backend.biz.kyzz.entity.KyzzUserQuestionBank;
import org.example.backend.biz.kyzz.mapper.KyzzQuestionBankMapper;
import org.example.backend.biz.kyzz.mapper.KyzzUserQuestionBankMapper;
import org.example.backend.biz.kyzz.support.KyzzPracticeSupport;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
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
    private static final Set<String> SUPPORTED_SELECTION_STATUS = Set.of("all", "selected", "unselected");

    private final KyzzQuestionBankMapper kyzzQuestionBankMapper;
    private final KyzzUserQuestionBankMapper kyzzUserQuestionBankMapper;
    private final KyzzPracticeSupport kyzzPracticeSupport;

    public KyzzQuestionBankUserService(KyzzQuestionBankMapper kyzzQuestionBankMapper,
                                       KyzzUserQuestionBankMapper kyzzUserQuestionBankMapper,
                                       KyzzPracticeSupport kyzzPracticeSupport) {
        this.kyzzQuestionBankMapper = kyzzQuestionBankMapper;
        this.kyzzUserQuestionBankMapper = kyzzUserQuestionBankMapper;
        this.kyzzPracticeSupport = kyzzPracticeSupport;
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

        Map<Long, KyzzCategory> categoryMap = kyzzPracticeSupport.buildCategoryMap();
        Map<Long, KyzzPracticeSupport.QuestionBankProgressSnapshot> progressMap = kyzzPracticeSupport.buildProgressSnapshotMap(userId, toBankIds(activeBanks), activeBanks);
        List<KyzzQuestionBankMineRecordResponse> records = activeBanks.stream()
                .map(bank -> toMineRecord(bank, relationMap.get(bank.getId()), categoryMap.get(bank.getCategoryId()), progressMap.get(bank.getId())))
                .sorted(Comparator
                        .comparing(KyzzQuestionBankMineRecordResponse::getLastPracticeAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(KyzzQuestionBankMineRecordResponse::getJoinedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        int selectedCount = records.size();
        int completedCount = (int) records.stream()
                .filter(item -> item.getQuestionCount() != null && item.getQuestionCount() > 0)
                .filter(item -> item.getCurrentProgress() != null && item.getCurrentProgress().compareTo(KyzzPracticeSupport.FULL_PROGRESS) >= 0)
                .count();
        int inProgressCount = (int) records.stream()
                .filter(item -> item.getCurrentProgress() != null)
                .filter(item -> item.getCurrentProgress().compareTo(KyzzPracticeSupport.ZERO_PROGRESS) > 0)
                .filter(item -> item.getCurrentProgress().compareTo(KyzzPracticeSupport.FULL_PROGRESS) < 0)
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
        Map<Long, KyzzCategory> categoryMap = kyzzPracticeSupport.buildCategoryMap();
        Map<Long, KyzzUserQuestionBank> selectedRelationMap = kyzzPracticeSupport.buildSelectedRelationMap(userId, toBankIds(activeBanks));
        Map<Long, KyzzPracticeSupport.QuestionBankProgressSnapshot> progressMap = kyzzPracticeSupport.buildProgressSnapshotMap(userId, selectedRelationMap.keySet(), activeBanks);

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

        KyzzQuestionBank bank = kyzzPracticeSupport.requireActiveQuestionBank(bankId);
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
                relation.setCurrentProgress(KyzzPracticeSupport.ZERO_PROGRESS);
                relation.setStudiedCount(0);
                relation.setCorrectCount(0);
                relation.setWrongCount(0);
                kyzzUserQuestionBankMapper.insert(relation);
            }
        } else if (relation != null) {
            kyzzUserQuestionBankMapper.deleteById(relation.getId());
            relation = null;
        }

        kyzzPracticeSupport.syncStudyUserCount(bankId);
        KyzzQuestionBank refreshedBank = kyzzPracticeSupport.requireActiveQuestionBank(bankId);
        KyzzCategory category = refreshedBank.getCategoryId() == null ? null : kyzzPracticeSupport.buildCategoryMap().get(refreshedBank.getCategoryId());

        if (relation != null) {
            Map<Long, KyzzPracticeSupport.QuestionBankProgressSnapshot> progressMap = kyzzPracticeSupport.buildProgressSnapshotMap(userId, List.of(bankId), List.of(refreshedBank));
            KyzzPracticeSupport.QuestionBankProgressSnapshot snapshot = progressMap.getOrDefault(bankId, KyzzPracticeSupport.QuestionBankProgressSnapshot.empty());
            kyzzPracticeSupport.syncRelationSnapshot(relation.getId(), snapshot);
            relation = kyzzUserQuestionBankMapper.selectById(relation.getId());
            return toPublicRecord(refreshedBank, relation, category, snapshot, true);
        }
        return toPublicRecord(refreshedBank, null, category, null, false);
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

    private KyzzQuestionBankMineRecordResponse toMineRecord(KyzzQuestionBank bank,
                                                            KyzzUserQuestionBank relation,
                                                            KyzzCategory category,
                                                            KyzzPracticeSupport.QuestionBankProgressSnapshot snapshot) {
        KyzzPracticeSupport.QuestionBankProgressSnapshot resolvedSnapshot = snapshot == null ? KyzzPracticeSupport.QuestionBankProgressSnapshot.empty() : snapshot;
        return new KyzzQuestionBankMineRecordResponse(
                bank.getId(),
                bank.getBankCode(),
                bank.getBankName(),
                bank.getSubtitle(),
                kyzzPracticeSupport.resolveCoverUrl(bank.getCoverUrl()),
                bank.getCategoryId(),
                category == null ? null : category.getCategoryName(),
                bank.getDifficultyLevel(),
                bank.getQuestionCount() == null ? 0 : bank.getQuestionCount(),
                kyzzPracticeSupport.normalizeScore(bank.getTotalScore()),
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
                                                                KyzzPracticeSupport.QuestionBankProgressSnapshot snapshot,
                                                                boolean selected) {
        KyzzPracticeSupport.QuestionBankProgressSnapshot resolvedSnapshot = selected
                ? (snapshot == null ? KyzzPracticeSupport.QuestionBankProgressSnapshot.empty() : snapshot)
                : null;
        return new KyzzQuestionBankPublicRecordResponse(
                bank.getId(),
                bank.getBankCode(),
                bank.getBankName(),
                bank.getSubtitle(),
                kyzzPracticeSupport.resolveCoverUrl(bank.getCoverUrl()),
                bank.getCategoryId(),
                category == null ? null : category.getCategoryName(),
                bank.getDifficultyLevel(),
                bank.getQuestionCount() == null ? 0 : bank.getQuestionCount(),
                kyzzPracticeSupport.normalizeScore(bank.getTotalScore()),
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

    private List<Long> toBankIds(Collection<KyzzQuestionBank> banks) {
        if (banks == null || banks.isEmpty()) {
            return List.of();
        }
        List<Long> result = new ArrayList<>(banks.size());
        banks.forEach(bank -> result.add(bank.getId()));
        return result;
    }
}
