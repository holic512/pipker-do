package org.example.backend.shared.config.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 索引: 系统配置变更日志实体。
 */
@Data
@TableName("sys_config_change_log")
public class SysConfigChangeLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String configGroup;

    private String configKey;

    private String oldValueMasked;

    private String newValueMasked;

    private Long changedBy;

    private String requestId;

    private LocalDateTime createdAt;
}
