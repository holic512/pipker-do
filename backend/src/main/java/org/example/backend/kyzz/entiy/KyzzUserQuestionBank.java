package org.example.backend.kyzz.entiy;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("kyzz_user_question_bank")
public class KyzzUserQuestionBank implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long questionBankId;

    private String joinSource;

    private BigDecimal currentProgress;

    private Integer studiedCount;

    private Integer correctCount;

    private Integer wrongCount;

    private LocalDateTime lastPracticeAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
