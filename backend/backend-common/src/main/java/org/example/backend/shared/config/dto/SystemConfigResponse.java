package org.example.backend.shared.config.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 索引: 后台系统配置项响应。
 */
@Data
@AllArgsConstructor
public class SystemConfigResponse {

    private Long id;

    private String configGroup;

    private String configKey;

    private String configName;

    private String configType;

    private String value;

    private String maskedValue;

    private Boolean sensitive;

    private Boolean hasValue;

    private Integer enabled;

    private String description;

    private Integer sortNo;

    private Long updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
