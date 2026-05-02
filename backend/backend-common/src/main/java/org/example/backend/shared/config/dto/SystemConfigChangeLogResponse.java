package org.example.backend.shared.config.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 索引: 后台系统配置变更日志响应。
 */
@Data
@AllArgsConstructor
public class SystemConfigChangeLogResponse {

    private Long id;

    private String configGroup;

    private String configKey;

    private String oldValueMasked;

    private String newValueMasked;

    private Long changedBy;

    private String requestId;

    private LocalDateTime createdAt;
}
