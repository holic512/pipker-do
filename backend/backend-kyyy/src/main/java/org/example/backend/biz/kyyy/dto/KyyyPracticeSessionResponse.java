/**
 * @file KyyyPracticeSessionResponse
 * @project pipker-do
 * @module 考研英语 / 背词会话
 * @description 统一返回当前学习会话的主状态、当前卡片、空态与完成态。
 * @logic 1. 返回会话状态与模式；2. 返回词库、进度与当前卡片；3. 返回空态和完成总结供前端渲染。
 * @dependencies DTO: KyyyPracticeSessionBankResponse, DTO: KyyyPracticeSessionProgressResponse, DTO: KyyyPracticeSessionCardResponse
 * @index_tags 考研英语, 会话响应, 背词接口, 学习状态
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class KyyyPracticeSessionResponse implements Serializable {

    private Long sessionId;

    private String mode;

    private String status;

    private KyyyPracticeSessionBankResponse bank;

    private KyyyPracticeSessionProgressResponse progressSummary;

    private KyyyPracticeSessionCardResponse currentCard;

    private KyyyPracticeSessionEmptyStateResponse emptyState;

    private KyyyPracticeSessionCompletionResponse completionSummary;
}
