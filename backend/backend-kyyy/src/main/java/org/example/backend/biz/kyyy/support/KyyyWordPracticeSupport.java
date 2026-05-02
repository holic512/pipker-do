package org.example.backend.biz.kyyy.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.biz.kyyy.entity.KyyyUserWordBank;
import org.example.backend.biz.kyyy.entity.KyyyWordBank;
import org.example.backend.biz.kyyy.mapper.KyyyUserWordBankMapper;
import org.example.backend.biz.kyyy.mapper.KyyyWordBankMapper;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * AI 索引: KYYY 词库与背词公共支持。
 */
@Component
public class KyyyWordPracticeSupport {

    private final KyyyWordBankMapper kyyyWordBankMapper;
    private final KyyyUserWordBankMapper kyyyUserWordBankMapper;

    public KyyyWordPracticeSupport(KyyyWordBankMapper kyyyWordBankMapper,
                                   KyyyUserWordBankMapper kyyyUserWordBankMapper) {
        this.kyyyWordBankMapper = kyyyWordBankMapper;
        this.kyyyUserWordBankMapper = kyyyUserWordBankMapper;
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

    public void syncStudyUserCount(Long bankId) {
        Long count = kyyyUserWordBankMapper.selectCount(new LambdaQueryWrapper<KyyyUserWordBank>()
                .eq(KyyyUserWordBank::getWordBankId, bankId));
        kyyyWordBankMapper.update(null, new LambdaUpdateWrapper<KyyyWordBank>()
                .eq(KyyyWordBank::getId, bankId)
                .set(KyyyWordBank::getStudyUserCount, count == null ? 0 : count.intValue()));
    }
}
