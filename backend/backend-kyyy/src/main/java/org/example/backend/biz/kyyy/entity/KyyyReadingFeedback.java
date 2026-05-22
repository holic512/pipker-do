/**
 * @file KyyyReadingFeedback
 * @project pipker-do
 * @module 考研英语 / 阅读模块
 * @description 映射阅读问题反馈工单表，承载文章级或题目级的问题上报与处理状态。
 * @logic 1. 绑定用户、文章和可选题目；2. 保存问题类型、反馈内容和联系方式；3. 记录后台处理结果。
 * @dependencies Table: kyyy_reading_feedback
 * @index_tags 考研英语, 阅读反馈, 问题上报, 工单
 * @author holic512
 */
package org.example.backend.biz.kyyy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("kyyy_reading_feedback")
public class KyyyReadingFeedback implements Serializable {

    /** 阅读反馈主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID。 */
    private Long userId;

    /** 阅读文章 ID。 */
    private Long passageId;

    /** 阅读题目 ID。 */
    private Long questionId;

    /** 阅读练习会话 ID。 */
    private Long sessionId;

    /** 反馈类型。 */
    private String feedbackType;

    /** 反馈内容。 */
    private String content;

    /** 联系方式。 */
    private String contactInfo;

    /** 处理状态。 */
    private String status;

    /** 后台处理回复。 */
    private String adminReply;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
