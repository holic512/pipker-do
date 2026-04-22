package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("kyzz_user_note")
public class KyzzUserNote implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long questionId;

    private Long questionBankId;

    private String noteTitle;

    private String noteContent;

    private Integer isPublic;

    private Integer likeCount;

    private Integer favoriteCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
