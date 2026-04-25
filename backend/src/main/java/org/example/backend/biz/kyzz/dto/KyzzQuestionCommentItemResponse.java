package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 索引: KYZZ 用户侧题目评论单项。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionCommentItemResponse implements Serializable {

    private Long commentId;

    private Long questionId;

    private String content;

    private String createdAt;

    private Integer likeCount;

    private Integer replyCount;

    private KyzzQuestionCommentAuthorResponse author;

    private Boolean isMine;

    private Boolean isLiked;
}
