package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 索引: KYZZ 用户侧题目评论作者信息。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionCommentAuthorResponse implements Serializable {

    private Long id;

    private String nickname;

    private String avatarUrl;
}
