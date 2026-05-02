package org.example.backend.biz.kyyy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 索引: KYYY 单词延伸关系实体。
 */
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

    /** 展示排序值。 */
    private Integer sortNo;

    /** 状态。 */
    private Integer status;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
