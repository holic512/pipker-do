package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 考研政治评论实体。
 */
@Data
@TableName("kyzz_comment")
public class KyzzComment implements Serializable {

    /** 评论主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 评论用户 ID。 */
    private Long userId;

    /** 评论目标类型。 */
    private String targetType;

    /** 评论目标 ID。 */
    private Long targetId;

    /** 父评论 ID，顶级评论为空。 */
    private Long parentId;

    /** 被回复用户 ID。 */
    private Long replyToUserId;

    /** 评论内容。 */
    private String content;

    /** 点赞数量。 */
    private Integer likeCount;

    /** 回复数量。 */
    private Integer replyCount;

    /** 评论状态。 */
    private Integer status;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
