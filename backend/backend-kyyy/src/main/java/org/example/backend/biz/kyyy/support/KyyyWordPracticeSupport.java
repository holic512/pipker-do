package org.example.backend.biz.kyyy.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.biz.kyyy.entity.KyyyUserPracticeSetting;
import org.example.backend.biz.kyyy.entity.KyyyUserWordBank;
import org.example.backend.biz.kyyy.entity.KyyyWordBank;
import org.example.backend.biz.kyyy.mapper.KyyyUserPracticeSettingMapper;
import org.example.backend.biz.kyyy.mapper.KyyyUserWordBankMapper;
import org.example.backend.biz.kyyy.mapper.KyyyWordBankMapper;
import org.example.backend.biz.kyyy.support.KyyyExamDirectionSupport;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AI 索引: KYYY 词库与背词公共支持。
 */
@Component
public class KyyyWordPracticeSupport {

    private final KyyyWordBankMapper kyyyWordBankMapper;
    private final KyyyUserWordBankMapper kyyyUserWordBankMapper;
    private final KyyyUserPracticeSettingMapper kyyyUserPracticeSettingMapper;

    public KyyyWordPracticeSupport(KyyyWordBankMapper kyyyWordBankMapper,
                                   KyyyUserWordBankMapper kyyyUserWordBankMapper,
                                   KyyyUserPracticeSettingMapper kyyyUserPracticeSettingMapper) {
        this.kyyyWordBankMapper = kyyyWordBankMapper;
        this.kyyyUserWordBankMapper = kyyyUserWordBankMapper;
        this.kyyyUserPracticeSettingMapper = kyyyUserPracticeSettingMapper;
    }

    public KyyyWordBank requireActiveWordBank(Long bankId) {
        if (bankId == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "词库不能为空");
        }
        KyyyWordBank bank = kyyyWordBankMapper.selectById(bankId);
        if (bank == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "词库不存在");
        }
        if (!Objects.equals(bank.getStatus(), 1)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "词库已下架，暂时无法操作");
        }
        return bank;
    }

    public KyyyWordBank requireActiveSelectedWordBank(Long userId, Long bankId) {
        if (bankId == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "词库不能为空");
        }
        KyyyWordBank bank = requireActiveWordBank(bankId);
        Long relationCount = kyyyUserWordBankMapper.selectCount(new LambdaQueryWrapper<KyyyUserWordBank>()
                .eq(KyyyUserWordBank::getUserId, userId)
                .eq(KyyyUserWordBank::getWordBankId, bankId));
        if (relationCount == null || relationCount <= 0) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "请先选择该词库后再设为默认");
        }
        return bank;
    }

    public void syncStudyUserCount(Long bankId) {
        Long count = kyyyUserWordBankMapper.selectCount(new LambdaQueryWrapper<KyyyUserWordBank>()
                .eq(KyyyUserWordBank::getWordBankId, bankId));
        kyyyWordBankMapper.update(null, new LambdaUpdateWrapper<KyyyWordBank>()
                .eq(KyyyWordBank::getId, bankId)
                .set(KyyyWordBank::getStudyUserCount, count == null ? 0 : count.intValue()));
    }

    public KyyyUserPracticeSetting loadPracticeSetting(Long userId) {
        if (userId == null) {
            return null;
        }
        return kyyyUserPracticeSettingMapper.selectOne(new LambdaQueryWrapper<KyyyUserPracticeSetting>()
                .eq(KyyyUserPracticeSetting::getUserId, userId)
                .last("limit 1"));
    }

    public List<KyyyUserWordBank> loadSelectedRelations(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return kyyyUserWordBankMapper.selectList(new LambdaQueryWrapper<KyyyUserWordBank>()
                .eq(KyyyUserWordBank::getUserId, userId)
                .orderByDesc(KyyyUserWordBank::getCreatedAt)
                .orderByDesc(KyyyUserWordBank::getId));
    }

    public Long resolveAvailableDefaultWordBankId(Long userId, Long currentDefaultWordBankId) {
        List<KyyyUserWordBank> selectedRelations = loadSelectedRelations(userId);
        if (selectedRelations.isEmpty()) {
            return null;
        }

        Map<Long, KyyyWordBank> activeBankMap = loadActiveBankMap(selectedRelations.stream()
                .map(KyyyUserWordBank::getWordBankId)
                .toList());
        if (activeBankMap.isEmpty()) {
            return null;
        }

        if (currentDefaultWordBankId != null && activeBankMap.containsKey(currentDefaultWordBankId)) {
            return currentDefaultWordBankId;
        }

        for (KyyyUserWordBank relation : selectedRelations) {
            if (activeBankMap.containsKey(relation.getWordBankId())) {
                return relation.getWordBankId();
            }
        }
        return null;
    }

    public KyyyUserPracticeSetting syncDefaultWordBankSelection(Long userId) {
        KyyyUserPracticeSetting setting = loadPracticeSetting(userId);
        Long resolvedDefaultWordBankId = resolveAvailableDefaultWordBankId(
                userId,
                setting == null ? null : setting.getDefaultWordBankId()
        );

        if (setting == null) {
            if (resolvedDefaultWordBankId == null) {
                return null;
            }
            setting = new KyyyUserPracticeSetting();
            setting.setUserId(userId);
            setting.setExamDirection(KyyyExamDirectionSupport.DEFAULT_DIRECTION);
            setting.setDefaultWordBankId(resolvedDefaultWordBankId);
            kyyyUserPracticeSettingMapper.insert(setting);
            return loadPracticeSetting(userId);
        }

        if (!Objects.equals(setting.getDefaultWordBankId(), resolvedDefaultWordBankId)) {
            setting.setDefaultWordBankId(resolvedDefaultWordBankId);
            kyyyUserPracticeSettingMapper.updateById(setting);
        }
        return setting;
    }

    public KyyyWordBank loadActiveSelectedWordBank(Long userId, Long bankId) {
        if (userId == null || bankId == null) {
            return null;
        }
        Long selectedCount = kyyyUserWordBankMapper.selectCount(new LambdaQueryWrapper<KyyyUserWordBank>()
                .eq(KyyyUserWordBank::getUserId, userId)
                .eq(KyyyUserWordBank::getWordBankId, bankId));
        if (selectedCount == null || selectedCount <= 0) {
            return null;
        }
        return requireActiveWordBank(bankId);
    }

    private Map<Long, KyyyWordBank> loadActiveBankMap(List<Long> bankIds) {
        if (bankIds == null || bankIds.isEmpty()) {
            return Map.of();
        }
        List<KyyyWordBank> activeBanks = kyyyWordBankMapper.selectList(new LambdaQueryWrapper<KyyyWordBank>()
                .in(KyyyWordBank::getId, bankIds)
                .eq(KyyyWordBank::getStatus, 1)
                .orderByAsc(KyyyWordBank::getSortNo)
                .orderByDesc(KyyyWordBank::getId));
        Map<Long, KyyyWordBank> bankMap = new LinkedHashMap<>();
        activeBanks.forEach(item -> bankMap.putIfAbsent(item.getId(), item));
        return bankMap;
    }
}
