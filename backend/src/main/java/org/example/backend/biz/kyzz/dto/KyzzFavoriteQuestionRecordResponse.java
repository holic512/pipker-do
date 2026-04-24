package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 索引: KYZZ 用户侧收藏题目记录。
 */
@Data
@AllArgsConstructor
public class KyzzFavoriteQuestionRecordResponse implements Serializable {

    private Long questionId;

    private Long bankId;

    private String bankName;

    private String questionType;

    private String stem;

    private Integer difficultyLevel;

    private LocalDateTime favoriteAt;
}
