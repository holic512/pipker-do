package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 索引: KYZZ 用户侧刷题会话进度。
 */
@Data
@AllArgsConstructor
public class KyzzPracticeSessionProgressResponse {

    private Integer currentQuestionIndex;

    private Integer totalQuestionCount;
}
