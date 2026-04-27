package org.example.backend.shared.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 索引: 后台 LLM 调用记录响应。
 */
@Data
@AllArgsConstructor
public class LlmCallRecordResponse {

    private Long id;

    private String scene;

    private String model;

    private String status;

    private Long latencyMs;

    private Integer inputTokens;

    private Integer outputTokens;

    private Integer totalTokens;

    private String requestId;

    private String promptHash;

    private String inputPreview;

    private String outputPreview;

    private String errorMessage;

    private Long operatorId;

    private LocalDateTime createdAt;
}
