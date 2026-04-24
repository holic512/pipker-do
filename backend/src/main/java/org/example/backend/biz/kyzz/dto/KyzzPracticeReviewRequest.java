package org.example.backend.biz.kyzz.dto;

import lombok.Data;

import java.util.List;

/**
 * AI 索引: KYZZ 用户侧刷题查看答案请求。
 */
@Data
public class KyzzPracticeReviewRequest {

    private Long bankId;

    private List<String> selectedOptionKeys;

    private String answerText;

    private Integer usedSeconds;

    private String sourceType;

    private String sourceStatus;

    private String keyword;
}
