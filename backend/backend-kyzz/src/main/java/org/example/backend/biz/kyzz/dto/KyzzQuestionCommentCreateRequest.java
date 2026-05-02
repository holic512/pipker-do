package org.example.backend.biz.kyzz.dto;

import lombok.Data;

/**
 * AI 索引: KYZZ 用户侧题目评论发布请求。
 */
@Data
public class KyzzQuestionCommentCreateRequest {

    private String content;
}
