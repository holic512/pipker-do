package org.example.backend.biz.kyyy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 索引: KYYY 用户单词进度实体。
 */
@Data
@TableName("kyyy_user_word_progress")
public class KyyyUserWordProgress implements Serializable {

    /** 进度主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID。 */
    private Long userId;

    /** 单词 ID。 */
    private Long wordId;

    /** 学习状态。 */
    private String studyStatus;

    /** 掌握等级。 */
    private Integer masteryLevel;

    /** 复习次数。 */
    private Integer reviewCount;

    /** 答对次数。 */
    private Integer correctCount;

    /** 答错次数。 */
    private Integer wrongCount;

    /** 最近结果。 */
    private String lastResult;

    /** 最近学习时间。 */
    private LocalDateTime lastStudiedAt;

    /** 下次复习时间。 */
    private LocalDateTime nextReviewAt;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
