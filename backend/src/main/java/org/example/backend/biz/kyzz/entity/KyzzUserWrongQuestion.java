package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("kyzz_user_wrong_question")
public class KyzzUserWrongQuestion implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long questionId;

    private Long questionBankId;

    private LocalDateTime firstWrongAt;

    private LocalDateTime lastWrongAt;

    private Integer wrongCount;

    private Integer isMastered;

    private LocalDateTime masteredAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
