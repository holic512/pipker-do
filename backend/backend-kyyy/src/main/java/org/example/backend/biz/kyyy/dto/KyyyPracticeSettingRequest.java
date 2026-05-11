package org.example.backend.biz.kyyy.dto;

import lombok.Data;

/**
 * AI 索引: KYYY 用户侧刷题设置更新请求。
 */
@Data
public class KyyyPracticeSettingRequest {

    private String examDirection;

    private Long defaultWordBankId;
}
