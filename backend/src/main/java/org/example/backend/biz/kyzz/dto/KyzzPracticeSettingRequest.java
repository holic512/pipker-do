package org.example.backend.biz.kyzz.dto;

import lombok.Data;

/**
 * AI 索引: KYZZ 用户侧刷题设置更新请求。
 */
@Data
public class KyzzPracticeSettingRequest {

    private Boolean autoJumpOnCorrect;
}
