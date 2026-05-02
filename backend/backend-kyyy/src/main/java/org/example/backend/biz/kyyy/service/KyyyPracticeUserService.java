package org.example.backend.biz.kyyy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.backend.biz.kyyy.dto.KyyyPracticeSettingRequest;
import org.example.backend.biz.kyyy.dto.KyyyPracticeSettingResponse;
import org.example.backend.biz.kyyy.entity.KyyyUserPracticeSetting;
import org.example.backend.biz.kyyy.mapper.KyyyUserPracticeSettingMapper;
import org.example.backend.biz.kyyy.support.KyyyExamDirectionSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI 索引: KYYY 用户侧刷题设置服务。
 */
@Service
public class KyyyPracticeUserService {

    private final KyyyUserPracticeSettingMapper kyyyUserPracticeSettingMapper;

    public KyyyPracticeUserService(KyyyUserPracticeSettingMapper kyyyUserPracticeSettingMapper) {
        this.kyyyUserPracticeSettingMapper = kyyyUserPracticeSettingMapper;
    }

    public KyyyPracticeSettingResponse getSettings(Long userId) {
        return toPracticeSettingResponse(loadPracticeSetting(userId));
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
