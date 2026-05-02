package org.example.backend.shared.account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 索引: VIP 兑换 Key 实体，保存后台可见明文 Key 与兑换状态。
 */
@Data
@TableName("vip_card_key")
public class VipCardKey implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long groupId;

    private String cardKey;

    private String batchNo;

    private Integer status;

    private Long redeemedUserId;

    private LocalDateTime redeemedAt;

    private LocalDateTime voidedAt;

    private String voidReason;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
