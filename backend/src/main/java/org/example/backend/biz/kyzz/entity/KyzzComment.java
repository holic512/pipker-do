package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("kyzz_comment")
public class KyzzComment implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String targetType;

    private Long targetId;

    private Long parentId;

    private Long replyToUserId;

    private String content;

    private Integer likeCount;

    private Integer replyCount;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
