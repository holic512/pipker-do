package org.example.backend.biz.kyyy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 索引: KYYY 单词实体。
 */
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

    /** 例句。 */
    private String exampleSentence;

    /** 例句翻译。 */
    private String exampleTranslation;

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
