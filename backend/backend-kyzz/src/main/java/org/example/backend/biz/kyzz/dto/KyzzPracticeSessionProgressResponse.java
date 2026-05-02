package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 索引: KYZZ 用户侧刷题会话进度。
 */
@Data
@AllArgsConstructor
public class KyzzPracticeSessionProgressResponse implements Serializable {

    private Integer currentQuestionIndex;

    private Integer totalQuestionCount;
}
