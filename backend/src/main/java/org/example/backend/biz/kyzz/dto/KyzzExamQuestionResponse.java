package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * AI 索引: KYZZ VIP 考试答题题目。
 */
@Data
@AllArgsConstructor
public class KyzzExamQuestionResponse implements Serializable {

    private Long id;

    private Long questionId;

    private Long bankId;

    private String questionType;

    private Integer questionOrder;

    private String stem;

    private Integer difficultyLevel;

    private BigDecimal score;

    private String sourceName;

    private Integer yearNo;

    private List<KyzzExamQuestionOptionResponse> options;

    private List<String> selectedOptionKeys;

    private String answerText;

    private Integer answerStatus;
}
