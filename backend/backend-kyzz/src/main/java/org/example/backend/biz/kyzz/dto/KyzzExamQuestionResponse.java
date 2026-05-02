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

    private BigDecimal awardedScore;

    private String sourceName;

    private Integer yearNo;

    private List<KyzzExamQuestionOptionResponse> options;

    private List<String> selectedOptionKeys;

    private List<String> correctOptionKeys;

    private String answerText;

    private Integer answerStatus;

    private Integer isCorrect;

    private String gradingStatus;

    private String gradingMethod;

    private String referenceAnswer;

    private String analysis;

    private String gradingComment;

    private BigDecimal gradingConfidence;

    private List<String> matchedPoints;

    private List<String> missingPoints;

    private Long llmRecordId;

    private String gradedAt;
}
