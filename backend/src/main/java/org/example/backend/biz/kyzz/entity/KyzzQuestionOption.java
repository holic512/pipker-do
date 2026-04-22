package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("kyzz_question_option")
public class KyzzQuestionOption implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionId;

    private String optionKey;

    private String optionContent;

    private Integer isCorrect;

    private Integer sortNo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
