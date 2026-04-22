package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("kyzz_question_bank_rating")
public class KyzzQuestionBankRating implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long questionBankId;

    private Integer score;

    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
