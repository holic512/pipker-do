package org.example.backend.biz.kyyy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 索引: KYYY 用户词库关系实体。
 */
@Data
@TableName("kyyy_user_word_bank")
public class KyyyUserWordBank implements Serializable {

    /** 关系主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID。 */
    private Long userId;

    /** 词库 ID。 */
    private Long wordBankId;

    /** 加入来源。 */
    private String joinSource;

    /** 最近背词时间。 */
    private LocalDateTime lastPracticeAt;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
