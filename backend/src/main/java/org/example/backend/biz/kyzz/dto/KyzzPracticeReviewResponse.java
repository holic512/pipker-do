package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI 索引: KYZZ 用户侧刷题查看答案结果。
 */
@Data
@AllArgsConstructor
public class KyzzPracticeReviewResponse implements Serializable {

    private Long questionId;

    private Long bankId;

    private String questionType;

    private List<String> submittedOptionKeys;

    private String submittedAnswerText;

    private Boolean requiresSelfJudgement;

    private Boolean isCorrect;

    private List<String> correctOptionKeys;

    private String answerText;

    private String analysis;

    private KyzzPracticeBankRecordResponse updatedBank;

    private Long nextQuestionId;

    private Integer nextQuestionIndex;

    private Boolean completedBank;

    private String sourceType;

    private String sourceTitle;

    private Boolean completedSource;
}
