package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 索引: KYZZ 题目管理列表项。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionAdminItemResponse {

    private Long id;

    private Long questionBankId;

    private String questionBankName;

    private Long categoryId;

    private String categoryName;

    private String questionType;

    private Integer difficultyLevel;

    private BigDecimal score;

    private String sourceName;

    private Integer yearNo;

    private Integer sortNo;

    private Integer status;

    private String stem;

    private String stemPreview;

    private String analysis;

    private String answerText;

    private List<String> correctOptionKeys;

    private Integer optionCount;

    private Boolean canDelete;

    private String deleteBlockReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
