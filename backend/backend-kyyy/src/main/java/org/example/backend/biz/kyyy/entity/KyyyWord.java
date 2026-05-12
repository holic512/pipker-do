/**
 * @file KyyyWord
 * @project pipker-do
 * @module 考研英语 / 单词词库
 * @description 映射 KYYY 单词主表，承载单词基础释义、音标、难度与启停状态。
 * @logic 1. 保存单词基础信息；2. 通过 normalizedWord 做唯一归一化；3. 例句改由 kyyy_word_example 独立维护。
 * @dependencies Table: kyyy_word
 * @index_tags 考研英语, 单词主表, 词库, 基础释义
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
@TableName("kyyy_word")
public class KyyyWord implements Serializable {

    /** 单词主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 单词原文。 */
    private String wordText;

    /** 标准化单词。 */
    private String normalizedWord;

    /** 美式音标。 */
    private String phoneticUs;

    /** 英式音标。 */
    private String phoneticUk;

    /** 词性。 */
    private String partOfSpeech;

    /** 中文释义。 */
    private String meaningCn;

    /** 难度等级。 */
    private Integer difficultyLevel;

    /** 状态。 */
    private Integer status;

    /** 创建人 ID。 */
    private Long createdBy;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
