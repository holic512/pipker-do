/**
 * @file KyyyReadingQuestion
 * @project pipker-do
 * @module 考研英语 / 阅读模块
 * @description 映射阅读文章下的小题记录，承载题干、答案、解析与分值信息。
 * @logic 1. 按 passageId 归属篇章；2. 用 questionNo 标记篇内题号；3. 保存标准答案、解析和难度。
 * @dependencies Table: kyyy_reading_question
 * @index_tags 考研英语, 阅读题目, 小题, 解析
 * @author holic512
 */
package org.example.backend.biz.kyyy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("kyyy_reading_question")
public class KyyyReadingQuestion implements Serializable {

    /** 阅读题目主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属阅读文章 ID。 */
    private Long passageId;

    /** 文章内题号。 */
    private Integer questionNo;

    /** 题型。 */
    private String questionType;

    /** 题干。 */
    private String stem;

    /** 标准答案文本。 */
    private String answerText;

    /** 题目解析。 */
    private String analysis;

    /** 难度等级。 */
    private Integer difficultyLevel;

    /** 分值。 */
    private BigDecimal score;

    /** 状态。 */
    private Integer status;

    /** 排序值。 */
    private Integer sortNo;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
