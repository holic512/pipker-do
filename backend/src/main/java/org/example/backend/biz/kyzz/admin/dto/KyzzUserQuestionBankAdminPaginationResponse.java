package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 索引: KYZZ 管理端用户题库选择分页信息。
 */
@Data
@AllArgsConstructor
public class KyzzUserQuestionBankAdminPaginationResponse {

    private Long pageNo;

    private Long pageSize;

    private Long total;

    private Long totalPages;
}
