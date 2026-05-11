package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI 索引: KYYY 用户侧刷题设置响应。
 */
@Data
@AllArgsConstructor
public class KyyyPracticeSettingResponse implements Serializable {

    private String examDirection;

    private String examDirectionLabel;

    private Long defaultWordBankId;

    private String defaultWordBankName;

    private List<KyyyPracticeSettingOptionResponse> examDirectionOptions;
}
