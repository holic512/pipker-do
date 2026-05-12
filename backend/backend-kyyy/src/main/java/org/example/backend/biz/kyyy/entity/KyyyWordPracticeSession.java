/**
 * @file KyyyWordPracticeSession
 * @project pipker-do
 * @module 考研英语 / 背词会话
 * @description 保存一次学习或复习会话的整体元数据与统计快照。
 * @logic 1. 绑定用户、默认词库与会话模式；2. 保存预算、进度与评分计数；3. 保存开始、完成与最后答题时间。
 * @dependencies Table: kyyy_word_practice_session
 * @index_tags 考研英语, 背词会话, 学习会话, 复习会话
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
@TableName("kyyy_word_practice_session")
public class KyyyWordPracticeSession implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long wordBankId;

    private String mode;

    private String status;

    private Integer totalCards;

    private Integer completedCards;

    private Integer knownCount;

    private Integer fuzzyCount;

    private Integer unknownCount;

    private LocalDateTime startedAt;

    private LocalDateTime lastAnsweredAt;

    private LocalDateTime finishedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
