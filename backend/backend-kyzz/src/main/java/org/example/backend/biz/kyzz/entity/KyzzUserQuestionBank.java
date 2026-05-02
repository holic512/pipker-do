package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户题库学习关系实体。
 */
@Data
@TableName("kyzz_user_question_bank")
public class KyzzUserQuestionBank implements Serializable {

    /** 关系主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID。 */
    private Long userId;

    /** 题库 ID。 */
    private Long questionBankId;

    /** 加入来源。 */
    private String joinSource;

    /** 当前学习进度。 */
    private BigDecimal currentProgress;

    /** 已学习题目数量。 */
    private Integer studiedCount;

    /** 答对题目数量。 */
    private Integer correctCount;

    /** 答错题目数量。 */
    private Integer wrongCount;

    /** 最近练习时间。 */
    private LocalDateTime lastPracticeAt;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
