package org.example.backend.shared.config.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 索引: 系统配置实体。
 */
@Data
@TableName("sys_config")
public class SysConfig implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String configGroup;

    private String configKey;

    private String configName;

    private String configType;

    private String configValue;

    private Integer sensitive;

    private Integer enabled;

    private String description;

    private Integer sortNo;

    private Long updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
