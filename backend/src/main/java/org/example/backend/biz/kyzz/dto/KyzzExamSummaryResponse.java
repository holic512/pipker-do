package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * AI 索引: KYZZ VIP 考试列表摘要。
 */
@Data
@AllArgsConstructor
public class KyzzExamSummaryResponse implements Serializable {

    private Long sessionId;

    private String examNo;

    private String examType;

    private String examTypeLabel;

    private String difficultyMode;

    private String difficultyLabel;

    private Integer durationMinutes;

    private Integer totalQuestionCount;

    private Integer answeredCount;

    private BigDecimal totalScore;

    private BigDecimal earnedScore;

    private BigDecimal objectiveScore;

    private BigDecimal subjectiveScore;

    private String status;

    private String statusLabel;

    private String gradingStatus;

    private String gradingStatusLabel;

    private String startedAt;

    private String deadlineAt;

    private String submittedAt;

    private String gradedAt;

    private String gradingErrorMessage;

    private Long remainingSeconds;
}
