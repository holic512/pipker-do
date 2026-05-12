/**
 * @file KyyyWordRelated
 * @project pipker-do
 * @module 考研英语 / 词库相关词
 * @description 映射 KYYY 单词延伸关系表，承载近义词、反义词、派生词等关系。
 * @logic 1. 绑定主单词与关联词；2. 记录关系类型、释义与展示排序；3. 标记 AI 补齐来源与运行批次。
 * @dependencies Table: kyyy_word_related
 * @index_tags 考研英语, 相关词, 近义词, AI补齐
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
@TableName("kyyy_word_related")
public class KyyyWordRelated implements Serializable {

    /** 延伸关系主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 主单词 ID。 */
    private Long wordId;

    /** 关联单词 ID。 */
    private Long relatedWordId;

    /** 延伸词原文。 */
    private String relatedWordText;

    /** 标准化延伸词。 */
    private String normalizedRelatedWord;

    /** 中文释义。 */
    private String meaningCn;

    /** 关系类型。 */
    private String relationType;

    /** 来源类型。 */
    private String sourceType;

    /** AI 补齐运行 ID。 */
    private String sourceRunId;

    /** 展示排序值。 */
    private Integer sortNo;

    /** 状态。 */
    private Integer status;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
