package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 索引: KYZZ VIP 考试试题快照实体。
 */
@Data
@TableName("kyzz_exam_question")
public class KyzzExamQuestion implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private Long questionId;

    private Long questionBankId;

    private String questionType;

    private Integer questionOrder;

    private BigDecimal score;

    private String answerContent;

    private Integer answerStatus;

    private Integer usedSeconds;

    private LocalDateTime answeredAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
