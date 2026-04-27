package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 索引: KYZZ VIP 考试会话实体。
 */
@Data
@TableName("kyzz_exam_session")
public class KyzzExamSession implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String examNo;

    private String examType;

    private String difficultyMode;

    private Integer durationMinutes;

    private Integer singleCount;

    private Integer multipleCount;

    private Integer shortCount;

    private Integer totalQuestionCount;

    private Integer answeredCount;

    private BigDecimal totalScore;

    private String status;

    private LocalDateTime startedAt;

    private LocalDateTime deadlineAt;

    private LocalDateTime submittedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
