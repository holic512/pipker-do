package org.example.backend.biz.kyyy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.backend.biz.kyyy.dto.KyyyPracticeNextWordResponse;
import org.example.backend.biz.kyyy.dto.KyyyRelatedWordResponse;
import org.example.backend.biz.kyyy.dto.KyyyPracticeSettingRequest;
import org.example.backend.biz.kyyy.dto.KyyyPracticeSettingResponse;
import org.example.backend.biz.kyyy.dto.KyyyWordSourceBankResponse;
import org.example.backend.biz.kyyy.entity.KyyyUserPracticeSetting;
import org.example.backend.biz.kyyy.entity.KyyyUserWordBank;
import org.example.backend.biz.kyyy.entity.KyyyUserWordProgress;
import org.example.backend.biz.kyyy.entity.KyyyWord;
import org.example.backend.biz.kyyy.entity.KyyyWordBank;
import org.example.backend.biz.kyyy.entity.KyyyWordBankWordRel;
import org.example.backend.biz.kyyy.entity.KyyyWordRelated;
import org.example.backend.biz.kyyy.mapper.KyyyUserPracticeSettingMapper;
import org.example.backend.biz.kyyy.mapper.KyyyUserWordBankMapper;
import org.example.backend.biz.kyyy.mapper.KyyyUserWordProgressMapper;
import org.example.backend.biz.kyyy.mapper.KyyyWordBankMapper;
import org.example.backend.biz.kyyy.mapper.KyyyWordBankWordRelMapper;
import org.example.backend.biz.kyyy.mapper.KyyyWordMapper;
import org.example.backend.biz.kyyy.mapper.KyyyWordRelatedMapper;
import org.example.backend.biz.kyyy.support.KyyyExamDirectionSupport;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private final KyyyUserPracticeSettingMapper kyyyUserPracticeSettingMapper;
    private final KyyyWordBankMapper kyyyWordBankMapper;
    private final KyyyWordMapper kyyyWordMapper;
    private final KyyyWordBankWordRelMapper kyyyWordBankWordRelMapper;
    private final KyyyUserWordBankMapper kyyyUserWordBankMapper;
    private final KyyyUserWordProgressMapper kyyyUserWordProgressMapper;
    private final KyyyWordRelatedMapper kyyyWordRelatedMapper;

    public KyyyPracticeUserService(KyyyUserPracticeSettingMapper kyyyUserPracticeSettingMapper,
                                   KyyyWordBankMapper kyyyWordBankMapper,
                                   KyyyWordMapper kyyyWordMapper,
                                   KyyyWordBankWordRelMapper kyyyWordBankWordRelMapper,
                                   KyyyUserWordBankMapper kyyyUserWordBankMapper,
                                   KyyyUserWordProgressMapper kyyyUserWordProgressMapper,
                                   KyyyWordRelatedMapper kyyyWordRelatedMapper) {
        this.kyyyUserPracticeSettingMapper = kyyyUserPracticeSettingMapper;
        this.kyyyWordBankMapper = kyyyWordBankMapper;
        this.kyyyWordMapper = kyyyWordMapper;
        this.kyyyWordBankWordRelMapper = kyyyWordBankWordRelMapper;
        this.kyyyUserWordBankMapper = kyyyUserWordBankMapper;
        this.kyyyUserWordProgressMapper = kyyyUserWordProgressMapper;
        this.kyyyWordRelatedMapper = kyyyWordRelatedMapper;
    }

    public KyyyPracticeSettingResponse getSettings(Long userId) {
        return toPracticeSettingResponse(loadPracticeSetting(userId));
    }

    public KyyyPracticeNextWordResponse getNextWord(Long userId) {
        List<KyyyUserWordBank> selectedRelations = kyyyUserWordBankMapper.selectList(new LambdaQueryWrapper<KyyyUserWordBank>()
                .eq(KyyyUserWordBank::getUserId, userId)
                .orderByDesc(KyyyUserWordBank::getCreatedAt)
                .orderByDesc(KyyyUserWordBank::getId));
        if (selectedRelations.isEmpty()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "请先选择词库后再开始背词");
        }

        Map<Long, KyyyUserWordBank> relationMap = new LinkedHashMap<>();
        selectedRelations.forEach(item -> relationMap.putIfAbsent(item.getWordBankId(), item));
        List<KyyyWordBank> activeBanks = kyyyWordBankMapper.selectList(new LambdaQueryWrapper<KyyyWordBank>()
                .in(KyyyWordBank::getId, relationMap.keySet())
                .eq(KyyyWordBank::getStatus, 1)
                .orderByAsc(KyyyWordBank::getSortNo)
                .orderByDesc(KyyyWordBank::getId));
        if (activeBanks.isEmpty()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前已选词库暂不可用");
        }

        List<KyyyWordBankWordRel> bankWordRelations = kyyyWordBankWordRelMapper.selectList(new LambdaQueryWrapper<KyyyWordBankWordRel>()
                .in(KyyyWordBankWordRel::getWordBankId, activeBanks.stream().map(KyyyWordBank::getId).toList())
                .orderByAsc(KyyyWordBankWordRel::getWordBankId)
                .orderByAsc(KyyyWordBankWordRel::getSortNo)
                .orderByAsc(KyyyWordBankWordRel::getId));
        if (bankWordRelations.isEmpty()) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "当前已选词库暂无可学习单词");
        }

        Map<Long, List<KyyyWordBankWordRel>> relationByBankId = new LinkedHashMap<>();
        bankWordRelations.forEach(item -> relationByBankId.computeIfAbsent(item.getWordBankId(), key -> new ArrayList<>()).add(item));

        LinkedHashSet<Long> orderedWordIds = new LinkedHashSet<>();
        Map<Long, List<KyyyWordBank>> sourceBanksByWordId = new LinkedHashMap<>();
        for (KyyyWordBank bank : activeBanks) {
            for (KyyyWordBankWordRel relation : relationByBankId.getOrDefault(bank.getId(), List.of())) {
                orderedWordIds.add(relation.getWordId());
                sourceBanksByWordId.computeIfAbsent(relation.getWordId(), key -> new ArrayList<>()).add(bank);
            }
        }
        if (orderedWordIds.isEmpty()) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "当前已选词库暂无可学习单词");
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
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "当前已选词库暂无可学习单词");
        }

        Map<Long, KyyyUserWordProgress> progressMap = buildUserWordProgressMap(userId, candidateWordIds);
        Long targetWordId = resolveNextWordId(candidateWordIds, progressMap);
        KyyyWord targetWord = wordMap.get(targetWordId);
        KyyyUserWordProgress progress = progressMap.get(targetWordId);
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
                sourceBanksByWordId.getOrDefault(targetWordId, List.of()).stream()
                        .map(bank -> new KyyyWordSourceBankResponse(bank.getId(), bank.getBankCode(), bank.getBankName()))
                        .toList(),
                relatedWords
        );
    }

    @Transactional
    public KyyyPracticeSettingResponse updateSettings(Long userId, KyyyPracticeSettingRequest request) {
        KyyyUserPracticeSetting existing = loadPracticeSetting(userId);
        if (existing == null) {
            KyyyUserPracticeSetting setting = new KyyyUserPracticeSetting();
            setting.setUserId(userId);
            setting.setExamDirection(KyyyExamDirectionSupport.normalizeOrDefault(
                    request == null ? null : request.getExamDirection()
            ));
            kyyyUserPracticeSettingMapper.insert(setting);
            return toPracticeSettingResponse(setting);
        }

        if (request != null && request.getExamDirection() != null) {
            existing.setExamDirection(KyyyExamDirectionSupport.normalize(request.getExamDirection()));
            kyyyUserPracticeSettingMapper.updateById(existing);
        }
        return toPracticeSettingResponse(existing);
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
        return kyyyUserPracticeSettingMapper.selectOne(new LambdaQueryWrapper<KyyyUserPracticeSetting>()
                .eq(KyyyUserPracticeSetting::getUserId, userId)
                .last("limit 1"));
    }

    private KyyyPracticeSettingResponse toPracticeSettingResponse(KyyyUserPracticeSetting setting) {
        String examDirection = KyyyExamDirectionSupport.normalizeOrDefault(
                setting == null ? null : setting.getExamDirection()
        );
        return new KyyyPracticeSettingResponse(
                examDirection,
                KyyyExamDirectionSupport.labelOf(examDirection),
                KyyyExamDirectionSupport.options()
        );
    }
}
