package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户笔记实体。
 */
@Data
@TableName("kyzz_user_note")
public class KyzzUserNote implements Serializable {

    /** 笔记主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID。 */
    private Long userId;

    /** 题目 ID。 */
    private Long questionId;

    /** 题库 ID。 */
    private Long questionBankId;

    /** 笔记标题。 */
    private String noteTitle;

    /** 笔记内容。 */
    private String noteContent;

    /** 是否公开。 */
    private Integer isPublic;

    /** 点赞数量。 */
    private Integer likeCount;

    /** 收藏数量。 */
    private Integer favoriteCount;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
