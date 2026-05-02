package org.example.backend.shared.llm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 索引: LLM 调用记录实体。
 */
@Data
@TableName("llm_call_record")
public class LlmCallRecord implements Serializable {

    @TableId(type = IdType.AUTO)
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
