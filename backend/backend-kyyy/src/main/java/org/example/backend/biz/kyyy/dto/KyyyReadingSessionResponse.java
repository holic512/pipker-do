/**
 * @file KyyyReadingSessionResponse
 * @project pipker-do
 * @module 考研英语 / 阅读做题
 * @description 统一返回阅读会话状态、篇章内容、题目列表、进度和交卷汇总。
 * @logic 1. 输出会话主状态；2. 一次性返回整篇文章与全部题目；3. 在已交卷时返回对错与解析。
 * @dependencies DTO: KyyyReadingPassageResponse, DTO: KyyyReadingProgressResponse, DTO: KyyyReadingQuestionResponse, DTO: KyyyReadingSummaryResponse
 * @index_tags 考研英语, 阅读会话, 整篇作答, 会话响应
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class KyyyReadingSessionResponse implements Serializable {

    private Long sessionId;

    private String status;

    private KyyyReadingPassageResponse passage;

    private KyyyReadingProgressResponse progress;

    private List<KyyyReadingQuestionResponse> questions;

    private KyyyReadingSummaryResponse summary;
}
