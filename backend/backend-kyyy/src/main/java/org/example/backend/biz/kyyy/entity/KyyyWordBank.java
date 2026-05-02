package org.example.backend.biz.kyyy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 索引: KYYY 词库实体。
 */
@Data
@TableName("kyyy_word_bank")
public class KyyyWordBank implements Serializable {

    /** 词库主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 词库编码。 */
    private String bankCode;

    /** 词库名称。 */
    private String bankName;

    /** 词库副标题。 */
    private String subtitle;

    /** 词库描述。 */
    private String description;

    /** 单词数量。 */
    private Integer wordCount;

    /** 学习人数。 */
    private Integer studyUserCount;

    /** 状态。 */
    private Integer status;

    /** 排序号。 */
    private Integer sortNo;

    /** 创建人 ID。 */
    private Long createdBy;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
