package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 索引: KYZZ 用户侧刷题题库记录。
 */
@Data
@AllArgsConstructor
public class KyzzPracticeBankRecordResponse implements Serializable {

    private Long bankId;

    private String bankName;

    private String coverUrl;

    private String categoryName;

    private Integer difficultyLevel;

    private Integer questionCount;

    private BigDecimal currentProgress;

    private Integer studiedCount;

    private Integer wrongCount;

    private LocalDateTime lastPracticeAt;

    private String resumeStatus;

    private String resumeLabel;

    private Long resumeQuestionId;

    private Integer resumeQuestionIndex;

    private LocalDateTime joinedAt;
}
