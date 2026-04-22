package org.example.backend.common.entiy;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("app_user_vip")
public class AppUserVip implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String vipType;

    private Integer vipStatus;

    private String sourceType;

    private BigDecimal amount;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
