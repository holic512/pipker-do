/**
 * @file KyyyReadingSession
 * @project pipker-do
 * @module 考研英语 / 阅读模块
 * @description 映射用户完成单篇阅读的一次练习会话，承载统计快照与提交状态。
 * @logic 1. 绑定用户与文章；2. 保存英一英二方向快照；3. 汇总答题数、正确数和正确率。
 * @dependencies Table: kyyy_reading_session
 * @index_tags 考研英语, 阅读会话, 练习历史, 会话统计
 * @author holic512
 */
package org.example.backend.biz.kyyy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("kyyy_reading_session")
public class KyyyReadingSession implements Serializable {

    /** 阅读会话主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID。 */
    private Long userId;

    /** 阅读文章 ID。 */
    private Long passageId;

    /** 作答时考试方向快照。 */
    private String examDirectionSnapshot;

    /** 会话状态。 */
    private String sessionStatus;

    /** 总题数。 */
    private Integer totalQuestionCount;

    /** 已作答题数。 */
    private Integer answeredCount;

    /** 答对题数。 */
    private Integer correctCount;

    /** 答错题数。 */
    private Integer wrongCount;

    /** 正确率百分比。 */
    private BigDecimal accuracyRate;

    /** 开始时间。 */
    private LocalDateTime startedAt;

    /** 提交时间。 */
    private LocalDateTime submittedAt;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
