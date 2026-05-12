/**
 * @file KyyyWordPracticeSessionItem
 * @project pipker-do
 * @module 考研英语 / 背词会话
 * @description 记录会话中的每一次出卡与回插结果，用于恢复与复盘学习流程。
 * @logic 1. 保存单次出卡的顺序、轮次与来源；2. 保存答题状态与评分结果；3. 保存展示与作答时间。
 * @dependencies Table: kyyy_word_practice_session_item
 * @index_tags 考研英语, 会话卡片, 回插队列, 学习记录
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
@TableName("kyyy_word_practice_session_item")
public class KyyyWordPracticeSessionItem implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private Long wordId;

    private String sourceType;

    private Integer roundNo;

    private Integer queueOrder;

    private String status;

    private String rating;

    private Integer scheduledAfterIndex;

    private LocalDateTime shownAt;

    private LocalDateTime answeredAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
