package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题库评分记录实体。
 */
@Data
@TableName("kyzz_question_bank_rating")
public class KyzzQuestionBankRating implements Serializable {

    /** 评分记录主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 评分用户 ID。 */
    private Long userId;

    /** 题库 ID。 */
    private Long questionBankId;

    /** 评分分数。 */
    private Integer score;

    /** 评分内容。 */
    private String content;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
