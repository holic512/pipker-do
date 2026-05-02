package org.example.backend.shared.llm.dto;

import lombok.Data;

/**
 * AI 索引: 后台 LLM 测试调用请求。
 */
@Data
public class AdminLlmTestRequest {

    private String mode;

    private String scene;

    private String systemPrompt;

    private String userPrompt;

    private String schemaName;

    private String jsonSchema;
}
