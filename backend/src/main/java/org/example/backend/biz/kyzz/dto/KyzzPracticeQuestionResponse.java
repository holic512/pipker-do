package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * AI 索引: KYZZ 用户侧刷题题目。
 */
@Data
@AllArgsConstructor
public class KyzzPracticeQuestionResponse {

    private Long id;

    private String questionType;

    private String stem;

    private Integer difficultyLevel;

    private BigDecimal score;

    private Integer sortNo;

    private String sourceName;

    private Integer yearNo;

    private List<KyzzPracticeQuestionOptionResponse> options;
}
