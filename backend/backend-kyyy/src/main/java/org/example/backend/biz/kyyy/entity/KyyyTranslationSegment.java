/**
 * @file KyyyTranslationSegment
 * @project pipker-do
 * @module 考研英语 / 翻译知识库
 * @description 映射翻译知识库分段表，保存英一 46-50 划线句或英二全文段的中英内容。
 * @logic 1. 绑定主表翻译题；2. 保存分段原文与对应译文；3. 支持按段号排序查询。
 * @dependencies Table: kyyy_translation_segment
 * @index_tags 考研英语, 翻译分段, 知识库, 真题, 子表
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
@TableName("kyyy_translation_segment")
public class KyyyTranslationSegment implements Serializable {

    /** 分段主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 翻译题 ID。 */
    private Long translationId;

    /** 分段唯一编码。 */
    private String segmentCode;

    /** 分段题号。 */
    private Integer segmentNo;

    /** 英文分段原文。 */
    private String sourceText;

    /** 中文分段译文。 */
    private String translatedText;

    /** 排序值。 */
    private Integer sortNo;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
