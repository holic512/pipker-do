package org.example.backend.biz.kyyy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 索引: KYYY 词库单词关联实体。
 */
@Data
@TableName("kyyy_word_bank_word_rel")
public class KyyyWordBankWordRel implements Serializable {

    /** 关联主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 词库 ID。 */
    private Long wordBankId;

    /** 单词 ID。 */
    private Long wordId;

    /** 词库内排序值。 */
    private Integer sortNo;

    /** 创建时间。 */
    private LocalDateTime createdAt;
}
