package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 索引: KYZZ 用户侧错题本记录。
 */
@Data
@AllArgsConstructor
public class KyzzWrongQuestionRecordResponse implements Serializable {

    private Long questionId;

    private Long bankId;

    private String bankName;

    private String questionType;

    private String stem;

    private Integer difficultyLevel;

    private Integer wrongCount;

    private LocalDateTime lastWrongAt;

    private Boolean isMastered;

    private LocalDateTime masteredAt;
}
