package org.example.backend.kyzz.entiy;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("kyzz_question_bank")
public class KyzzQuestionBank implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String bankCode;

    private String bankName;

    private String subtitle;

    private String coverUrl;

    private String description;

    private Long categoryId;

    private Integer difficultyLevel;

    private Integer questionCount;

    private BigDecimal totalScore;

    private Integer ratingCount;

    private Integer collectCount;

    private Integer studyUserCount;

    private Integer status;

    private Integer sortNo;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
