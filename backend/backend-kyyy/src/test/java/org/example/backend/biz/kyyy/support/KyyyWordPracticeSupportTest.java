package org.example.backend.biz.kyyy.support;

import org.example.backend.biz.kyyy.entity.KyyyUserPracticeSetting;
import org.example.backend.biz.kyyy.entity.KyyyUserWordBank;
import org.example.backend.biz.kyyy.entity.KyyyWordBank;
import org.example.backend.biz.kyyy.mapper.KyyyUserPracticeSettingMapper;
import org.example.backend.biz.kyyy.mapper.KyyyUserWordBankMapper;
import org.example.backend.biz.kyyy.mapper.KyyyWordBankMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KyyyWordPracticeSupportTest {

    @Mock
    private KyyyWordBankMapper kyyyWordBankMapper;

    @Mock
    private KyyyUserWordBankMapper kyyyUserWordBankMapper;

    @Mock
    private KyyyUserPracticeSettingMapper kyyyUserPracticeSettingMapper;

    private KyyyWordPracticeSupport kyyyWordPracticeSupport;

    @BeforeEach
    void setUp() {
        kyyyWordPracticeSupport = new KyyyWordPracticeSupport(
                kyyyWordBankMapper,
                kyyyUserWordBankMapper,
                kyyyUserPracticeSettingMapper
        );
    }

    @Test
    void syncDefaultWordBankSelectionCreatesDefaultFromLatestSelectedBank() {
        when(kyyyUserPracticeSettingMapper.selectOne(any())).thenReturn(null, createSetting(99L, 2L));
        when(kyyyUserWordBankMapper.selectList(any())).thenReturn(List.of(
                createRelation(42L, 2L, LocalDateTime.of(2026, 5, 4, 10, 0)),
                createRelation(42L, 1L, LocalDateTime.of(2026, 5, 4, 9, 0))
        ));
        when(kyyyWordBankMapper.selectList(any())).thenReturn(List.of(
                createBank(1L, 1),
                createBank(2L, 2)
        ));

        KyyyUserPracticeSetting result = kyyyWordPracticeSupport.syncDefaultWordBankSelection(42L);

        assertEquals(2L, result.getDefaultWordBankId());
        assertEquals("english_one", result.getExamDirection());
        verify(kyyyUserPracticeSettingMapper).insert(any(KyyyUserPracticeSetting.class));
    }

    @Test
    void syncDefaultWordBankSelectionFallsBackWhenCurrentDefaultBecomesInvalid() {
        when(kyyyUserPracticeSettingMapper.selectOne(any())).thenReturn(createSetting(88L, 1L));
        when(kyyyUserWordBankMapper.selectList(any())).thenReturn(List.of(
                createRelation(42L, 2L, LocalDateTime.of(2026, 5, 4, 10, 0))
        ));
        when(kyyyWordBankMapper.selectList(any())).thenReturn(List.of(
                createBank(2L, 1)
        ));

        KyyyUserPracticeSetting result = kyyyWordPracticeSupport.syncDefaultWordBankSelection(42L);

        assertEquals(2L, result.getDefaultWordBankId());
        verify(kyyyUserPracticeSettingMapper).updateById(any(KyyyUserPracticeSetting.class));
    }

    @Test
    void resolveAvailableDefaultWordBankIdReturnsNullWhenNoActiveSelectionExists() {
        when(kyyyUserWordBankMapper.selectList(any())).thenReturn(List.of());

        Long result = kyyyWordPracticeSupport.resolveAvailableDefaultWordBankId(42L, 9L);

        assertNull(result);
        verify(kyyyWordBankMapper, never()).selectList(any());
    }

    private KyyyUserPracticeSetting createSetting(Long id, Long defaultWordBankId) {
        KyyyUserPracticeSetting setting = new KyyyUserPracticeSetting();
        setting.setId(id);
        setting.setUserId(42L);
        setting.setExamDirection("english_one");
        setting.setDefaultWordBankId(defaultWordBankId);
        return setting;
    }

    private KyyyUserWordBank createRelation(Long userId, Long bankId, LocalDateTime createdAt) {
        KyyyUserWordBank relation = new KyyyUserWordBank();
        relation.setUserId(userId);
        relation.setWordBankId(bankId);
        relation.setCreatedAt(createdAt);
        return relation;
    }

    private KyyyWordBank createBank(Long id, int status) {
        KyyyWordBank bank = new KyyyWordBank();
        bank.setId(id);
        bank.setStatus(status);
        return bank;
    }
}
