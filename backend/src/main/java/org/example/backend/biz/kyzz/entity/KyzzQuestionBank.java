package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考研政治题库实体。
 */
@Data
@TableName("kyzz_question_bank")
public class KyzzQuestionBank implements Serializable {

    /** 题库主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 题库编码。 */
    private String bankCode;

    /** 题库名称。 */
    private String bankName;

    /** 题库副标题。 */
    private String subtitle;

    /** 题库封面地址。 */
    private String coverUrl;

    /** 题库描述。 */
    private String description;

    /** 所属分类 ID。 */
    private Long categoryId;

    /** 难度等级。 */
    private Integer difficultyLevel;

    /** 题目总数。 */
    private Integer questionCount;

    /** 总分值。 */
    private BigDecimal totalScore;

    /** 评分人数。 */
    private Integer ratingCount;

    /** 收藏人数。 */
    private Integer collectCount;

    /** 学习人数。 */
    private Integer studyUserCount;

    /** 题库状态。 */
    private Integer status;

    /** 排序号。 */
    private Integer sortNo;

    /** 创建人 ID。 */
    private Long createdBy;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
