package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 索引: KYZZ 题目管理选项出参。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionAdminOptionResponse {

    private Long id;

    private String optionKey;

    private String optionContent;

    private Integer isCorrect;

    private Integer sortNo;
}
