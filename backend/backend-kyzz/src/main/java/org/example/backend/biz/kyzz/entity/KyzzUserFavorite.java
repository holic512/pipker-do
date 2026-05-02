package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户收藏记录实体。
 */
@Data
@TableName("kyzz_user_favorite")
public class KyzzUserFavorite implements Serializable {

    /** 收藏记录主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID。 */
    private Long userId;

    /** 收藏目标 ID。 */
    private Long targetId;

    /** 创建时间。 */
    private LocalDateTime createdAt;
}
