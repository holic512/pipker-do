package org.example.backend.shared.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 索引: 公共 LLM 生成结果。
 */
@Data
@AllArgsConstructor
public class LlmGenerateResult {

    private String content;

    private String jsonContent;

    private String model;

    private LlmUsageResponse usage;

    private Long latencyMs;

    private Long recordId;

    private String requestId;
}
