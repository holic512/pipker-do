/**
 * @file KyyyTranslationPassage
 * @project pipker-do
 * @module 考研英语 / 翻译知识库
 * @description 映射翻译知识库主表，承载英一英二历年翻译题整题内容与来源信息。
 * @logic 1. 按方向、年份与唯一编码归档整题；2. 保存原文、译文、说明和来源；3. 维护段落数与检索元数据。
 * @dependencies Table: kyyy_translation_passage
 * @index_tags 考研英语, 翻译, 知识库, 真题, 主表
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
@TableName("kyyy_translation_passage")
public class KyyyTranslationPassage implements Serializable {

    /** 翻译题主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 翻译题唯一编码。 */
    private String translationCode;

    /** 考试方向。 */
    private String examDirection;

    /** 来源年份。 */
    private Integer sourceYear;

    /** 翻译模式：segmented/passage。 */
    private String translationMode;

    /** 数据标题。 */
    private String sourceTitle;

    /** 分值。 */
    private Integer scoreValue;

    /** 分段数量。 */
    private Integer segmentCount;

    /** 题目说明。 */
    private String promptInstruction;

    /** 英文原文。 */
    private String promptContent;

    /** 中文题目说明。 */
    private String promptTranslation;

    /** 中文参考译文全文。 */
    private String referenceTranslation;

    /** 译文备注。 */
    private String referenceNote;

    /** 检索标签。 */
    private String knowledgeTags;

    /** 来源 Markdown 路径。 */
    private String sourcePath;

    /** 原文来源链接。 */
    private String sourcePromptRef;

    /** 译文来源链接。 */
    private String sourceAnswerRef;

    /** 状态。 */
    private Integer status;

    /** 排序值。 */
    private Integer sortNo;

    /** 创建后台管理员 ID。 */
    private Long createdBy;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
