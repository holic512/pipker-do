package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 索引: KYZZ 管理端用户题库选择可选题库项。
 */
@Data
@AllArgsConstructor
public class KyzzUserQuestionBankAdminBankOptionResponse {

    private Long questionBankId;

    private String bankCode;

    private String bankName;

    private String categoryName;

    private Integer difficultyLevel;

    private Integer questionCount;

    private Integer sortNo;

    private Boolean selected;
}
