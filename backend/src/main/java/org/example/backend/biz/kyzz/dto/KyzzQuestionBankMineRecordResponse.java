package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 索引: KYZZ 用户侧我的题库卡片。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionBankMineRecordResponse implements Serializable {

    private Long id;

    private String bankCode;

    private String bankName;

    private String subtitle;

    private String coverUrl;

    private Long categoryId;

    private String categoryName;

    private Integer difficultyLevel;

    private Integer questionCount;

    private BigDecimal totalScore;

    private Integer ratingCount;

    private Integer studyUserCount;

    private BigDecimal currentProgress;

    private Integer studiedCount;

    private Integer correctCount;

    private Integer wrongCount;

    private LocalDateTime lastPracticeAt;

    private String joinSource;

    private LocalDateTime joinedAt;
}
