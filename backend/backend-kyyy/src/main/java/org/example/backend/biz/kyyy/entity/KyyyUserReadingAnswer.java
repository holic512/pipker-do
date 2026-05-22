/**
 * @file KyyyUserReadingAnswer
 * @project pipker-do
 * @module 考研英语 / 阅读模块
 * @description 映射用户阅读历史作答表，保存每次会话每道题的答案与题面快照。
 * @logic 1. 绑定 sessionId、userId 与 questionId；2. 保存题干、选项、答案和解析快照；3. 保存对错结果与耗时。
 * @dependencies Table: kyyy_user_reading_answer
 * @index_tags 考研英语, 阅读答题记录, 历史快照, 作答明细
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
@TableName("kyyy_user_reading_answer")
public class KyyyUserReadingAnswer implements Serializable {

    /** 阅读答题记录主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 阅读练习会话 ID。 */
    private Long sessionId;

    /** 用户 ID。 */
    private Long userId;

    /** 阅读文章 ID。 */
    private Long passageId;

    /** 阅读题目 ID。 */
    private Long questionId;

    /** 题号快照。 */
    private Integer questionNoSnapshot;

    /** 题干快照。 */
    private String questionStemSnapshot;

    /** 选项快照 JSON。 */
    private String optionSnapshotJson;

    /** 用户答案。 */
    private String answerContent;

    /** 标准答案快照。 */
    private String correctAnswerSnapshot;

    /** 解析快照。 */
    private String analysisSnapshot;

    /** 是否答对。 */
    private Integer isCorrect;

    /** 作答状态。 */
    private String answerStatus;

    /** 耗时秒数。 */
    private Integer usedSeconds;

    /** 提交时间。 */
    private LocalDateTime submittedAt;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
