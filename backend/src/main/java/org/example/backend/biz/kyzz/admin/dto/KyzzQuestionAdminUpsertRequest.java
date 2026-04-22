package org.example.backend.biz.kyzz.admin.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * AI 索引: KYZZ 题目管理新增编辑入参。
 */
@Data
public class KyzzQuestionAdminUpsertRequest {

    private Long questionBankId;

    private Long categoryId;

    private String questionType;

    private String stem;

    private String analysis;

    private String answerText;

    private Integer difficultyLevel;

    private BigDecimal score;

    private String sourceName;

    private Integer yearNo;

    private Integer sortNo;

    private Integer status;

    private List<KyzzQuestionAdminOptionRequest> options;
}
