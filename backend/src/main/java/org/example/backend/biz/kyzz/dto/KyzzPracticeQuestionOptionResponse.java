package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 索引: KYZZ 用户侧刷题选项。
 */
@Data
@AllArgsConstructor
public class KyzzPracticeQuestionOptionResponse {

    private String optionKey;

    private String optionContent;
}
