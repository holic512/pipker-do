package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题目与标签关联实体。
 */
@Data
@TableName("kyzz_question_tag_rel")
public class KyzzQuestionTagRel implements Serializable {

    /** 关联主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 题目 ID。 */
    private Long questionId;

    /** 标签 ID。 */
    private Long tagId;

    /** 创建时间。 */
    private LocalDateTime createdAt;
}
