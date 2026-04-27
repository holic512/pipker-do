package org.example.backend.shared.config.dto;

import lombok.Data;

/**
 * AI 索引: 后台系统配置更新请求。
 */
@Data
public class SystemConfigUpdateRequest {

    private String value;

    private Boolean keepSensitiveValue;

    private Integer enabled;
}
