package org.example.backend.shared.account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 索引: VIP 兑换卡组实体，后台按卡组批量生成兑换 Key。
 */
@Data
@TableName("vip_card_group")
public class VipCardGroup implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String groupName;

    private String vipType;

    private Integer durationDays;

    private Integer status;

    private String remark;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
