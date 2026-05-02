package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考研政治题目实体。
 */
@Data
@TableName("kyzz_question")
public class KyzzQuestion implements Serializable {

    /** 题目主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属题库 ID。 */
    private Long questionBankId;

    /** 所属分类 ID。 */
    private Long categoryId;

    /** 题目类型。 */
    private String questionType;

    /** 题干内容。 */
    private String stem;

    /** 题目解析。 */
    private String analysis;

    /** 标准答案文本。 */
    private String answerText;

    /** 难度等级。 */
    private Integer difficultyLevel;

    /** 题目分值。 */
    private BigDecimal score;

    /** 来源名称。 */
    private String sourceName;

    /** 年份。 */
    private Integer yearNo;

    /** 排序号。 */
    private Integer sortNo;

    /** 题目状态。 */
    private Integer status;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
