package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题目选项实体。
 */
@Data
@TableName("kyzz_question_option")
public class KyzzQuestionOption implements Serializable {

    /** 选项主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 题目 ID。 */
    private Long questionId;

    /** 选项标识，如 A、B、C、D。 */
    private String optionKey;

    /** 选项内容。 */
    private String optionContent;

    /** 是否为正确选项。 */
    private Integer isCorrect;

    /** 排序号。 */
    private Integer sortNo;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
