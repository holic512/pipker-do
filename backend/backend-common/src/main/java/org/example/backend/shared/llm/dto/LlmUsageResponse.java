package org.example.backend.shared.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 索引: LLM token 使用量响应。
 */
@Data
@AllArgsConstructor
public class LlmUsageResponse {

    private Integer inputTokens;

    private Integer outputTokens;

    private Integer totalTokens;
}
