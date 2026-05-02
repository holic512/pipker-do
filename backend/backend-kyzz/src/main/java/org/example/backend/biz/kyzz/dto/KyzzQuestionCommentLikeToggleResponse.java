package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 索引: KYZZ 用户侧题目评论点赞切换结果。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionCommentLikeToggleResponse implements Serializable {

    private Long commentId;

    private Long questionId;

    private Boolean isLiked;

    private Integer likeCount;
}
