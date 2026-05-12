/**
 * @file KyyyPracticeFeedbackRequest
 * @project pipker-do
 * @module 考研英语 / 背词会话
 * @description 承接用户对当前单词卡片的学习反馈。
 * @logic 1. 识别当前单词；2. 接收认识程度评分；3. 接收揭晓状态与响应耗时。
 * @dependencies API: /api/kyyy/practice/session/{sessionId}/feedback
 * @index_tags 考研英语, 学习反馈, 背词评分, 会话提交
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.Data;

@Data
public class KyyyPracticeFeedbackRequest {

    private Long wordId;

    private String rating;

    private Boolean revealed;

    private Long responseDurationMs;
}
