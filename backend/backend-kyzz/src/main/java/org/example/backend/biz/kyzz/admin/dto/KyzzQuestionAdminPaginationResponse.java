package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 索引: KYZZ 题目管理分页信息。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionAdminPaginationResponse {

    private Long pageNo;

    private Long pageSize;

    private Long total;

    private Long totalPages;
}
