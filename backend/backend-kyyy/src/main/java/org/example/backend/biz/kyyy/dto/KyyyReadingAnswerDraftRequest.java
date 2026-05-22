/**
 * @file KyyyReadingAnswerDraftRequest
 * @project pipker-do
 * @module 考研英语 / 阅读做题
 * @description 承接用户在阅读会话中保存单题答案草稿的请求参数。
 * @logic 1. 接收当前选择的选项键；2. 接收当前题累计耗时；3. 供服务端更新作答草稿与进度。
 * @dependencies API: /api/kyyy/reading/session/{sessionId}/answers/{questionId}
 * @index_tags 考研英语, 阅读做题, 草稿保存, 单题作答
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.Data;

@Data
public class KyyyReadingAnswerDraftRequest {

    private String answerContent;

    private Integer usedSeconds;
}
