package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 索引: KYZZ 用户侧公共题库卡片。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionBankPublicRecordResponse {

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

    private Integer sortNo;

    private BigDecimal currentProgress;

    private Integer studiedCount;

    private Integer correctCount;

    private Integer wrongCount;

    private LocalDateTime lastPracticeAt;

    private String joinSource;

    private LocalDateTime joinedAt;

    private Boolean selected;
}
