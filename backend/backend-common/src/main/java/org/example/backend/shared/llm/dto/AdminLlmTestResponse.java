package org.example.backend.shared.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 索引: 后台 LLM 测试调用响应。
 */
@Data
@AllArgsConstructor
public class AdminLlmTestResponse {

    private String mode;

    private LlmGenerateResult result;
}
