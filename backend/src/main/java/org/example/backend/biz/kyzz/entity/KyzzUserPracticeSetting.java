package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 索引: KYZZ 用户刷题设置实体。
 */
@Data
@TableName("kyzz_user_practice_setting")
public class KyzzUserPracticeSetting implements Serializable {

    /** 设置主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID。 */
    private Long userId;

    /** 答案正确后是否自动进入下一题：0否 1是。 */
    private Integer autoJumpOnCorrect;

    /** 刷自选题库时是否只刷选择题：0否 1是。 */
    private Integer bankPracticeChoiceOnly;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
