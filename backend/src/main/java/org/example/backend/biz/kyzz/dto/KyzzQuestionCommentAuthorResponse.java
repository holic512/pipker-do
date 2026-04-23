package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 索引: KYZZ 用户侧题目评论作者信息。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionCommentAuthorResponse {

    private Long id;

    private String nickname;

    private String avatarUrl;
}
