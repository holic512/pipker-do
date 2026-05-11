package org.example.backend.biz.kyyy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.backend.biz.kyyy.dto.KyyyHomeDashboardResponse;
import org.example.backend.biz.kyyy.dto.KyyyPracticeNextWordResponse;
import org.example.backend.biz.kyyy.dto.KyyyPracticeSettingRequest;
import org.example.backend.biz.kyyy.dto.KyyyPracticeSettingResponse;
import org.example.backend.biz.kyyy.dto.KyyyRelatedWordResponse;
import org.example.backend.biz.kyyy.dto.KyyyWordSourceBankResponse;
import org.example.backend.biz.kyyy.entity.KyyyUserPracticeSetting;
import org.example.backend.biz.kyyy.entity.KyyyUserWordProgress;
import org.example.backend.biz.kyyy.entity.KyyyWord;
import org.example.backend.biz.kyyy.entity.KyyyWordBank;
import org.example.backend.biz.kyyy.entity.KyyyWordBankWordRel;
import org.example.backend.biz.kyyy.entity.KyyyWordRelated;
import org.example.backend.biz.kyyy.mapper.KyyyUserPracticeSettingMapper;
import org.example.backend.biz.kyyy.mapper.KyyyUserWordProgressMapper;
import org.example.backend.biz.kyyy.mapper.KyyyWordBankWordRelMapper;
import org.example.backend.biz.kyyy.mapper.KyyyWordMapper;
import org.example.backend.biz.kyyy.mapper.KyyyWordRelatedMapper;
import org.example.backend.biz.kyyy.support.KyyyExamDirectionSupport;
import org.example.backend.biz.kyyy.support.KyyyWordPracticeSupport;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * AI 索引: KYYY 用户侧刷题设置服务。
 */
@Service
public class KyyyPracticeUserService {

    private static final String DEFAULT_STUDY_STATUS = "new";
    private static final KyyyHomeDashboardResponse EMPTY_HOME_DASHBOARD =
            new KyyyHomeDashboardResponse(0, 0, null, null);

    private final KyyyUserPracticeSettingMapper kyyyUserPracticeSettingMapper;
    private final KyyyWordMapper kyyyWordMapper;
    private final KyyyWordBankWordRelMapper kyyyWordBankWordRelMapper;
    private final KyyyUserWordProgressMapper kyyyUserWordProgressMapper;
    private final KyyyWordRelatedMapper kyyyWordRelatedMapper;
    private final KyyyWordPracticeSupport kyyyWordPracticeSupport;

    public KyyyPracticeUserService(KyyyUserPracticeSettingMapper kyyyUserPracticeSettingMapper,
                                   KyyyWordMapper kyyyWordMapper,
                                   KyyyWordBankWordRelMapper kyyyWordBankWordRelMapper,
                                   KyyyUserWordProgressMapper kyyyUserWordProgressMapper,
                                   KyyyWordRelatedMapper kyyyWordRelatedMapper,
                                   KyyyWordPracticeSupport kyyyWordPracticeSupport) {
        this.kyyyUserPracticeSettingMapper = kyyyUserPracticeSettingMapper;
        this.kyyyWordMapper = kyyyWordMapper;
        this.kyyyWordBankWordRelMapper = kyyyWordBankWordRelMapper;
        this.kyyyUserWordProgressMapper = kyyyUserWordProgressMapper;
        this.kyyyWordRelatedMapper = kyyyWordRelatedMapper;
        this.kyyyWordPracticeSupport = kyyyWordPracticeSupport;
    }

    public KyyyPracticeSettingResponse getSettings(Long userId) {
        KyyyUserPracticeSetting setting = kyyyWordPracticeSupport.syncDefaultWordBankSelection(userId);
        KyyyWordBank defaultWordBank = setting == null
                ? null
                : kyyyWordPracticeSupport.loadActiveSelectedWordBank(userId, setting.getDefaultWordBankId());
        return toPracticeSettingResponse(setting, defaultWordBank);
    }

    public KyyyHomeDashboardResponse getHomeDashboard(Long userId) {
        KyyyUserPracticeSetting setting = kyyyWordPracticeSupport.syncDefaultWordBankSelection(userId);
        KyyyWordBank defaultWordBank = setting == null
                ? null
                : kyyyWordPracticeSupport.loadActiveSelectedWordBank(userId, setting.getDefaultWordBankId());
        if (defaultWordBank == null) {
            return EMPTY_HOME_DASHBOARD;
        }

        CandidateWordBundle bundle;
        try {
            bundle = buildCandidateWordBundle(userId, defaultWordBank);
        } catch (BusinessException error) {
            return new KyyyHomeDashboardResponse(0, 0, defaultWordBank.getId(), defaultWordBank.getBankName());
        }
        LocalDateTime now = LocalDateTime.now();
        int studyCount = 0;
        int reviewCount = 0;
        for (Long wordId : bundle.candidateWordIds()) {
            KyyyUserWordProgress progress = bundle.progressMap().get(wordId);
            if (!isStudied(progress)) {
                studyCount++;
                continue;
            }
            if (progress != null && progress.getNextReviewAt() != null && !progress.getNextReviewAt().isAfter(now)) {
                reviewCount++;
            }
        }

        return new KyyyHomeDashboardResponse(
                studyCount,
                reviewCount,
                defaultWordBank.getId(),
                defaultWordBank.getBankName()
        );
    }

    public KyyyPracticeNextWordResponse getNextWord(Long userId) {
        KyyyUserPracticeSetting setting = kyyyWordPracticeSupport.syncDefaultWordBankSelection(userId);
        KyyyWordBank defaultWordBank = setting == null
                ? null
                : kyyyWordPracticeSupport.loadActiveSelectedWordBank(userId, setting.getDefaultWordBankId());
        if (defaultWordBank == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "请先选择默认词库后再开始背词");
        }
        CandidateWordBundle bundle = buildCandidateWordBundle(userId, defaultWordBank);
        Long targetWordId = resolveNextWordId(bundle.candidateWordIds(), bundle.progressMap());
        return buildNextWordResponse(targetWordId, bundle);
    }

    @Transactional
    public KyyyPracticeSettingResponse updateSettings(Long userId, KyyyPracticeSettingRequest request) {
        KyyyUserPracticeSetting existing = loadPracticeSetting(userId);
        KyyyWordBank requestedDefaultWordBank = request == null || request.getDefaultWordBankId() == null
                ? null
                : kyyyWordPracticeSupport.requireActiveSelectedWordBank(userId, request.getDefaultWordBankId());
        if (existing == null) {
            KyyyUserPracticeSetting setting = new KyyyUserPracticeSetting();
            setting.setUserId(userId);
            setting.setExamDirection(KyyyExamDirectionSupport.normalizeOrDefault(
                    request == null ? null : request.getExamDirection()
            ));
            setting.setDefaultWordBankId(requestedDefaultWordBank == null ? null : requestedDefaultWordBank.getId());
            kyyyUserPracticeSettingMapper.insert(setting);
            KyyyUserPracticeSetting normalizedSetting = kyyyWordPracticeSupport.syncDefaultWordBankSelection(userId);
            KyyyWordBank defaultWordBank = normalizedSetting == null
                    ? null
                    : kyyyWordPracticeSupport.loadActiveSelectedWordBank(userId, normalizedSetting.getDefaultWordBankId());
            return toPracticeSettingResponse(normalizedSetting == null ? setting : normalizedSetting, defaultWordBank);
        }

        if (request != null) {
            if (request.getExamDirection() != null) {
                existing.setExamDirection(KyyyExamDirectionSupport.normalize(request.getExamDirection()));
            }
            if (request.getDefaultWordBankId() != null) {
                existing.setDefaultWordBankId(requestedDefaultWordBank.getId());
            }
            kyyyUserPracticeSettingMapper.updateById(existing);
        }
        KyyyUserPracticeSetting normalizedSetting = kyyyWordPracticeSupport.syncDefaultWordBankSelection(userId);
        KyyyWordBank defaultWordBank = normalizedSetting == null
                ? null
                : kyyyWordPracticeSupport.loadActiveSelectedWordBank(userId, normalizedSetting.getDefaultWordBankId());
        return toPracticeSettingResponse(normalizedSetting == null ? existing : normalizedSetting, defaultWordBank);
    }

    private CandidateWordBundle buildCandidateWordBundle(Long userId, KyyyWordBank defaultWordBank) {
        if (defaultWordBank == null || defaultWordBank.getId() == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "请先选择默认词库后再开始背词");
        }

        List<KyyyWordBankWordRel> bankWordRelations = kyyyWordBankWordRelMapper.selectList(new LambdaQueryWrapper<KyyyWordBankWordRel>()
                .eq(KyyyWordBankWordRel::getWordBankId, defaultWordBank.getId())
                .orderByAsc(KyyyWordBankWordRel::getSortNo)
                .orderByAsc(KyyyWordBankWordRel::getId));
        if (bankWordRelations.isEmpty()) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "当前默认词库暂无可学习单词");
        }

        LinkedHashSet<Long> orderedWordIds = new LinkedHashSet<>();
        Map<Long, List<KyyyWordBank>> sourceBanksByWordId = new LinkedHashMap<>();
        for (KyyyWordBankWordRel relation : bankWordRelations) {
            orderedWordIds.add(relation.getWordId());
            sourceBanksByWordId.put(relation.getWordId(), List.of(defaultWordBank));
        }
        if (orderedWordIds.isEmpty()) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "当前默认词库暂无可学习单词");
        }

        List<KyyyWord> activeWords = kyyyWordMapper.selectList(new LambdaQueryWrapper<KyyyWord>()
                .in(KyyyWord::getId, orderedWordIds)
                .eq(KyyyWord::getStatus, 1));
        Map<Long, KyyyWord> wordMap = new LinkedHashMap<>();
        activeWords.forEach(item -> wordMap.putIfAbsent(item.getId(), item));

        List<Long> candidateWordIds = orderedWordIds.stream()
                .filter(wordMap::containsKey)
                .toList();
        if (candidateWordIds.isEmpty()) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "当前默认词库暂无可学习单词");
        }

        Map<Long, KyyyUserWordProgress> progressMap = buildUserWordProgressMap(userId, candidateWordIds);
        return new CandidateWordBundle(candidateWordIds, wordMap, sourceBanksByWordId, progressMap);
    }

    private KyyyPracticeNextWordResponse buildNextWordResponse(Long targetWordId, CandidateWordBundle bundle) {
        KyyyWord targetWord = bundle.wordMap().get(targetWordId);
        KyyyUserWordProgress progress = bundle.progressMap().get(targetWordId);
        List<KyyyRelatedWordResponse> relatedWords = buildRelatedWordResponses(targetWordId);
        return new KyyyPracticeNextWordResponse(
                targetWord.getId(),
                targetWord.getWordText(),
                targetWord.getPhoneticUs(),
                targetWord.getPhoneticUk(),
                targetWord.getPartOfSpeech(),
                targetWord.getMeaningCn(),
                targetWord.getExampleSentence(),
                targetWord.getExampleTranslation(),
                targetWord.getDifficultyLevel(),
                progress == null || !StringUtils.hasText(progress.getStudyStatus()) ? DEFAULT_STUDY_STATUS : progress.getStudyStatus(),
                progress == null || progress.getMasteryLevel() == null ? 0 : progress.getMasteryLevel(),
                progress == null || progress.getReviewCount() == null ? 0 : progress.getReviewCount(),
                progress == null || progress.getCorrectCount() == null ? 0 : progress.getCorrectCount(),
                progress == null || progress.getWrongCount() == null ? 0 : progress.getWrongCount(),
                progress == null ? null : progress.getLastResult(),
                progress == null ? null : progress.getLastStudiedAt(),
                progress == null ? null : progress.getNextReviewAt(),
                bundle.sourceBanksByWordId().getOrDefault(targetWordId, List.of()).stream()
                        .map(bank -> new KyyyWordSourceBankResponse(bank.getId(), bank.getBankCode(), bank.getBankName()))
                        .toList(),
                relatedWords
        );
    }

    private Map<Long, KyyyUserWordProgress> buildUserWordProgressMap(Long userId, List<Long> wordIds) {
        if (wordIds == null || wordIds.isEmpty()) {
            return Map.of();
        }
        List<KyyyUserWordProgress> progresses = kyyyUserWordProgressMapper.selectList(new LambdaQueryWrapper<KyyyUserWordProgress>()
                .eq(KyyyUserWordProgress::getUserId, userId)
                .in(KyyyUserWordProgress::getWordId, wordIds)
                .orderByDesc(KyyyUserWordProgress::getUpdatedAt)
                .orderByDesc(KyyyUserWordProgress::getId));
        Map<Long, KyyyUserWordProgress> progressMap = new LinkedHashMap<>();
        progresses.forEach(item -> progressMap.putIfAbsent(item.getWordId(), item));
        return progressMap;
    }

    private List<KyyyRelatedWordResponse> buildRelatedWordResponses(Long wordId) {
        List<KyyyWordRelated> relations = kyyyWordRelatedMapper.selectList(new LambdaQueryWrapper<KyyyWordRelated>()
                .eq(KyyyWordRelated::getWordId, wordId)
                .eq(KyyyWordRelated::getStatus, 1)
                .orderByAsc(KyyyWordRelated::getSortNo)
                .orderByAsc(KyyyWordRelated::getId));
        return relations.stream()
                .map(item -> new KyyyRelatedWordResponse(
                        item.getId(),
                        item.getRelatedWordId(),
                        item.getRelatedWordText(),
                        item.getMeaningCn(),
                        item.getRelationType()
                ))
                .toList();
    }

    private Long resolveNextWordId(List<Long> candidateWordIds, Map<Long, KyyyUserWordProgress> progressMap) {
        return candidateWordIds.stream()
                .min(Comparator
                        .comparing((Long wordId) -> isStudied(progressMap.get(wordId)))
                        .thenComparing(wordId -> progressMap.get(wordId) == null ? null : progressMap.get(wordId).getNextReviewAt(),
                                Comparator.nullsLast(LocalDateTime::compareTo))
                        .thenComparing(wordId -> progressMap.get(wordId) == null ? null : progressMap.get(wordId).getLastStudiedAt(),
                                Comparator.nullsFirst(LocalDateTime::compareTo)))
                .orElseThrow(() -> new BusinessException(ApiResponseCode.NOT_FOUND, "当前已选词库暂无可学习单词"));
    }

    private boolean isStudied(KyyyUserWordProgress progress) {
        if (progress == null) {
            return false;
        }
        if (progress.getLastStudiedAt() != null) {
            return true;
        }
        if (progress.getReviewCount() != null && progress.getReviewCount() > 0) {
            return true;
        }
        if (progress.getCorrectCount() != null && progress.getCorrectCount() > 0) {
            return true;
        }
        if (progress.getWrongCount() != null && progress.getWrongCount() > 0) {
            return true;
        }
        if (StringUtils.hasText(progress.getLastResult())) {
            return true;
        }
        return StringUtils.hasText(progress.getStudyStatus()) && !DEFAULT_STUDY_STATUS.equalsIgnoreCase(progress.getStudyStatus());
    }

    private KyyyUserPracticeSetting loadPracticeSetting(Long userId) {
        return kyyyWordPracticeSupport.loadPracticeSetting(userId);
    }

    private KyyyPracticeSettingResponse toPracticeSettingResponse(KyyyUserPracticeSetting setting, KyyyWordBank defaultWordBank) {
        String examDirection = KyyyExamDirectionSupport.normalizeOrDefault(
                setting == null ? null : setting.getExamDirection()
        );
        return new KyyyPracticeSettingResponse(
                examDirection,
                KyyyExamDirectionSupport.labelOf(examDirection),
                defaultWordBank == null ? null : defaultWordBank.getId(),
                defaultWordBank == null ? null : defaultWordBank.getBankName(),
                KyyyExamDirectionSupport.options()
        );
    }

    private record CandidateWordBundle(List<Long> candidateWordIds,
                                       Map<Long, KyyyWord> wordMap,
                                       Map<Long, List<KyyyWordBank>> sourceBanksByWordId,
                                       Map<Long, KyyyUserWordProgress> progressMap) {
    }
}
