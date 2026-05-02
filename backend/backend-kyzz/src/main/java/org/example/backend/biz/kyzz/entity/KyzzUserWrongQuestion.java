package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户错题记录实体。
 */
@Data
@TableName("kyzz_user_wrong_question")
public class KyzzUserWrongQuestion implements Serializable {

    /** 错题记录主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID。 */
    private Long userId;

    /** 题目 ID。 */
    private Long questionId;

    /** 题库 ID。 */
    private Long questionBankId;

    /** 首次答错时间。 */
    private LocalDateTime firstWrongAt;

    /** 最近一次答错时间。 */
    private LocalDateTime lastWrongAt;

    /** 答错次数。 */
    private Integer wrongCount;

    /** 是否已掌握。 */
    private Integer isMastered;

    /** 掌握时间。 */
    private LocalDateTime masteredAt;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
