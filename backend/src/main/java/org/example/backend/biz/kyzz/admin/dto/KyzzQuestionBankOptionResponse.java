package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 索引: KYZZ 题目管理题库选项。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionBankOptionResponse {

    private Long id;

    private String bankCode;

    private String bankName;

    private Long categoryId;

    private String categoryName;

    private Integer status;
}
