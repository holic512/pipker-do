package org.example.backend.biz.kyyy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.backend.biz.kyyy.dto.KyyyWordBankListResponse;
import org.example.backend.biz.kyyy.dto.KyyyWordBankRecordResponse;
import org.example.backend.biz.kyyy.dto.KyyyWordBankSelectionRequest;
import org.example.backend.biz.kyyy.dto.KyyyWordBankSummaryResponse;
import org.example.backend.biz.kyyy.entity.KyyyUserPracticeSetting;
import org.example.backend.biz.kyyy.entity.KyyyUserWordBank;
import org.example.backend.biz.kyyy.entity.KyyyWordBank;
import org.example.backend.biz.kyyy.mapper.KyyyUserWordBankMapper;
import org.example.backend.biz.kyyy.mapper.KyyyWordBankMapper;
import org.example.backend.biz.kyyy.support.KyyyWordPracticeSupport;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AI 索引: KYYY 用户侧词库服务。
 */
@Service
public class KyyyWordBankUserService {

    private final KyyyWordBankMapper kyyyWordBankMapper;
    private final KyyyUserWordBankMapper kyyyUserWordBankMapper;
    private final KyyyWordPracticeSupport kyyyWordPracticeSupport;

    public KyyyWordBankUserService(KyyyWordBankMapper kyyyWordBankMapper,
                                   KyyyUserWordBankMapper kyyyUserWordBankMapper,
                                   KyyyWordPracticeSupport kyyyWordPracticeSupport) {
        this.kyyyWordBankMapper = kyyyWordBankMapper;
        this.kyyyUserWordBankMapper = kyyyUserWordBankMapper;
        this.kyyyWordPracticeSupport = kyyyWordPracticeSupport;
    }

    public KyyyWordBankListResponse getWordBanks(Long userId) {
        List<KyyyWordBank> activeBanks = kyyyWordBankMapper.selectList(new LambdaQueryWrapper<KyyyWordBank>()
                .eq(KyyyWordBank::getStatus, 1)
                .orderByAsc(KyyyWordBank::getSortNo)
                .orderByDesc(KyyyWordBank::getStudyUserCount)
                .orderByDesc(KyyyWordBank::getId));
        if (activeBanks.isEmpty()) {
            return new KyyyWordBankListResponse(new KyyyWordBankSummaryResponse(0, 0, null), List.of());
        }

        KyyyUserPracticeSetting practiceSetting = kyyyWordPracticeSupport.loadPracticeSetting(userId);
        Long defaultWordBankId = kyyyWordPracticeSupport.resolveAvailableDefaultWordBankId(
                userId,
                practiceSetting == null ? null : practiceSetting.getDefaultWordBankId()
        );
        Map<Long, KyyyUserWordBank> relationMap = buildSelectedRelationMap(
                userId,
                activeBanks.stream().map(KyyyWordBank::getId).toList()
        );
        List<KyyyWordBankRecordResponse> records = activeBanks.stream()
                .map(bank -> toRecord(
                        bank,
                        relationMap.get(bank.getId()),
                        relationMap.containsKey(bank.getId()),
                        Objects.equals(defaultWordBankId, bank.getId())
                ))
                .toList();
        return new KyyyWordBankListResponse(
                new KyyyWordBankSummaryResponse(records.size(), relationMap.size(), defaultWordBankId),
                records
        );
    }

    @Transactional
    public KyyyWordBankRecordResponse updateWordBankSelection(Long userId,
                                                              Long bankId,
                                                              KyyyWordBankSelectionRequest request) {
        if (request == null || request.getSelected() == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "请选择要执行的词库操作");
        }

        kyyyWordPracticeSupport.requireActiveWordBank(bankId);
        KyyyUserWordBank relation = kyyyUserWordBankMapper.selectOne(new LambdaQueryWrapper<KyyyUserWordBank>()
                .eq(KyyyUserWordBank::getUserId, userId)
                .eq(KyyyUserWordBank::getWordBankId, bankId)
                .last("limit 1"));
        if (Boolean.TRUE.equals(request.getSelected())) {
            if (relation == null) {
                relation = new KyyyUserWordBank();
                relation.setUserId(userId);
                relation.setWordBankId(bankId);
                relation.setJoinSource("manual");
                kyyyUserWordBankMapper.insert(relation);
                relation = kyyyUserWordBankMapper.selectById(relation.getId());
            }
        } else if (relation != null) {
            kyyyUserWordBankMapper.deleteById(relation.getId());
            relation = null;
        }

        KyyyUserPracticeSetting practiceSetting = kyyyWordPracticeSupport.syncDefaultWordBankSelection(userId);
        kyyyWordPracticeSupport.syncStudyUserCount(bankId);
        KyyyWordBank refreshedBank = kyyyWordPracticeSupport.requireActiveWordBank(bankId);
        return toRecord(
                refreshedBank,
                relation,
                relation != null,
                practiceSetting != null && Objects.equals(practiceSetting.getDefaultWordBankId(), refreshedBank.getId())
        );
    }

    private Map<Long, KyyyUserWordBank> buildSelectedRelationMap(Long userId, List<Long> bankIds) {
        if (bankIds == null || bankIds.isEmpty()) {
            return Map.of();
        }
        List<KyyyUserWordBank> relations = kyyyUserWordBankMapper.selectList(new LambdaQueryWrapper<KyyyUserWordBank>()
                .eq(KyyyUserWordBank::getUserId, userId)
                .in(KyyyUserWordBank::getWordBankId, bankIds)
                .orderByDesc(KyyyUserWordBank::getCreatedAt)
                .orderByDesc(KyyyUserWordBank::getId));
        Map<Long, KyyyUserWordBank> relationMap = new LinkedHashMap<>();
        relations.forEach(item -> relationMap.putIfAbsent(item.getWordBankId(), item));
        return relationMap;
    }

    private KyyyWordBankRecordResponse toRecord(KyyyWordBank bank,
                                                KyyyUserWordBank relation,
                                                boolean selected,
                                                boolean isDefault) {
        return new KyyyWordBankRecordResponse(
                bank.getId(),
                bank.getBankCode(),
                bank.getBankName(),
                bank.getSubtitle(),
                bank.getDescription(),
                bank.getWordCount() == null ? 0 : bank.getWordCount(),
                bank.getStudyUserCount() == null ? 0 : bank.getStudyUserCount(),
                bank.getSortNo() == null ? 0 : bank.getSortNo(),
                selected && relation != null ? relation.getJoinSource() : null,
                selected && relation != null ? relation.getCreatedAt() : null,
                selected && relation != null ? relation.getLastPracticeAt() : null,
                selected,
                selected && isDefault
        );
    }
}
