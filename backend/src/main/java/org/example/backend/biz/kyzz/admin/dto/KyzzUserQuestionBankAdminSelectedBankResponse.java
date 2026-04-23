package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 索引: KYZZ 管理端用户题库选择详情中的已选题库。
 */
@Data
@AllArgsConstructor
public class KyzzUserQuestionBankAdminSelectedBankResponse {

    private Long questionBankId;

    private String bankCode;

    private String bankName;

    private String categoryName;

    private Integer difficultyLevel;

    private Integer questionCount;

    private BigDecimal currentProgress;

    private Integer studiedCount;

    private Integer correctCount;

    private Integer wrongCount;

    private LocalDateTime lastPracticeAt;

    private String joinSource;

    private LocalDateTime joinedAt;

    private Integer status;
}
