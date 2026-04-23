package org.example.backend.shared.security.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 索引: 反扒风险事件表实体。
 */
@Data
@TableName("app_security_risk_event")
public class AppSecurityRiskEvent implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String deviceId;

    private String ip;

    private String path;

    private String method;

    private String ruleCode;

    private String action;

    private Integer riskScore;

    private String requestId;

    private String tokenHash;

    private String payloadJson;

    private LocalDateTime createdAt;
}
