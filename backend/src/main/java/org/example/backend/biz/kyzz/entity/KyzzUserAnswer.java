package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户答题记录实体。
 */
@Data
@TableName("kyzz_user_answer")
public class KyzzUserAnswer implements Serializable {

    /** 答题记录主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID。 */
    private Long userId;

    /** 题目 ID。 */
    private Long questionId;

    /** 题库 ID。 */
    private Long questionBankId;

    /** 用户答案内容。 */
    private String answerContent;

    /** 是否答对。 */
    private Integer isCorrect;

    /** 作答状态。 */
    private Integer answerStatus;

    /** 作答耗时，单位秒。 */
    private Integer usedSeconds;

    /** 提交时间。 */
    private LocalDateTime submittedAt;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
