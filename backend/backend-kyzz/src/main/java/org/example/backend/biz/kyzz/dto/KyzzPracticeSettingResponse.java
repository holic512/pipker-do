package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 索引: KYZZ 用户侧刷题设置响应。
 */
@Data
@AllArgsConstructor
public class KyzzPracticeSettingResponse implements Serializable {

    private Boolean autoJumpOnCorrect;

    private Boolean bankPracticeChoiceOnly;
}
