/**
 * @file KyyyWritingEssay
 * @project pipker-do
 * @module 考研英语 / 作文知识库
 * @description 映射作文知识库主表，承载英一英二历年大小作文题目、范文及双语内容。
 * @logic 1. 按考试方向、年份、大小作文唯一归档；2. 保存题目与范文中英双语文本；3. 保存题型、分值与字数等检索元数据。
 * @dependencies Table: kyyy_writing_essay
 * @index_tags 考研英语, 作文, 知识库, 范文, 真题
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
@TableName("kyyy_writing_essay")
public class KyyyWritingEssay implements Serializable {

    /** 作文主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作文唯一编码。 */
    private String writingCode;

    /** 考试方向。 */
    private String examDirection;

    /** 来源年份。 */
    private Integer sourceYear;

    /** 作文分区：small/big。 */
    private String essaySection;

    /** 题目类型：email/chart/picture 等。 */
    private String promptCategory;

    /** 数据标题。 */
    private String sourceTitle;

    /** 分值。 */
    private Integer scoreValue;

    /** 字数下限。 */
    private Integer wordLimitMin;

    /** 字数上限。 */
    private Integer wordLimitMax;

    /** 英文题目。 */
    private String promptContent;

    /** 英文范文。 */
    private String sampleContent;

    /** 中文题目翻译。 */
    private String promptTranslation;

    /** 中文范文翻译。 */
    private String sampleTranslation;

    /** 检索标签。 */
    private String knowledgeTags;

    /** 来源 Markdown 路径。 */
    private String sourcePath;

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
