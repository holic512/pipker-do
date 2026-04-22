package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论点赞记录实体。
 */
@Data
@TableName("kyzz_comment_like")
public class KyzzCommentLike implements Serializable {

    /** 点赞记录主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 点赞用户 ID。 */
    private Long userId;

    /** 被点赞评论 ID。 */
    private Long commentId;

    /** 创建时间。 */
    private LocalDateTime createdAt;
}
