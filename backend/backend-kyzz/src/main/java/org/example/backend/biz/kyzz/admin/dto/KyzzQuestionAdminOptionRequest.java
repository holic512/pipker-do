package org.example.backend.biz.kyzz.admin.dto;

import lombok.Data;

/**
 * AI 索引: KYZZ 题目管理选项入参。
 */
@Data
public class KyzzQuestionAdminOptionRequest {

    private String optionKey;

    private String optionContent;

    private Integer isCorrect;
}
