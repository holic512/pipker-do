/**
 * @file KyyyWordExample
 * @project pipker-do
 * @module 考研英语 / 词库例句
 * @description 映射 KYYY 单词多例句表，承载 AI 或人工补充的英文例句与中文翻译。
 * @logic 1. 按 wordId 归属单词；2. 使用 exampleHash 去重；3. 通过 sortNo 和 status 控制展示顺序与启停。
 * @dependencies Table: kyyy_word_example
 * @index_tags 考研英语, 单词例句, 多例句, AI补齐
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
@TableName("kyyy_word_example")
public class KyyyWordExample implements Serializable {

    /** 例句主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 单词 ID。 */
    private Long wordId;

    /** 英文例句。 */
    private String exampleSentence;

    /** 例句中文翻译。 */
    private String exampleTranslation;

    /** 例句标准化 SHA-256。 */
    private String exampleHash;

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
