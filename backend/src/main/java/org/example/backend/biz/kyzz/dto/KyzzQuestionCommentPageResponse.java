package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * AI 索引: KYZZ 用户侧题目评论分页结果。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionCommentPageResponse {

    private List<KyzzQuestionCommentItemResponse> records;

    private Long pageNo;

    private Long pageSize;

    private Boolean hasMore;

    private Long total;
}
