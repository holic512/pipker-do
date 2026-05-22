/**
 * @file KyyyReadingQuestionOption
 * @project pipker-do
 * @module 考研英语 / 阅读模块
 * @description 映射阅读题选项表，承载选择题选项内容、顺序与正确标记。
 * @logic 1. 按 questionId 归属题目；2. 用 optionKey 标记选项；3. 通过 isCorrect 支撑答案校验。
 * @dependencies Table: kyyy_reading_question_option
 * @index_tags 考研英语, 阅读选项, 选择题, 题目选项
 * @author holic512
 */
package org.example.backend.biz.kyyy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("kyyy_reading_question_option")
public class KyyyReadingQuestionOption implements Serializable {

    /** 阅读题选项主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 阅读题目 ID。 */
    private Long questionId;

    /** 选项标识。 */
    private String optionKey;

    /** 选项内容。 */
    private String optionContent;

    /** 是否正确。 */
    private Integer isCorrect;

    /** 排序值。 */
    private Integer sortNo;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
