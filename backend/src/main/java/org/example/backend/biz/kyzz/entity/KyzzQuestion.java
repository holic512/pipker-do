package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("kyzz_question")
public class KyzzQuestion implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionBankId;

    private Long categoryId;

    private String questionType;

    private String stem;

    private String analysis;

    private String answerText;

    private Integer difficultyLevel;

    private BigDecimal score;

    private String sourceName;

    private Integer yearNo;

    private Integer sortNo;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
