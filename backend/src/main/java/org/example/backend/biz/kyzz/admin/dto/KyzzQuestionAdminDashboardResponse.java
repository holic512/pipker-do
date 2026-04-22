package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * AI 索引: KYZZ 题目管理首页数据。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionAdminDashboardResponse {

    private KyzzQuestionAdminStatsResponse stats;

    private List<KyzzQuestionAdminItemResponse> records;

    private KyzzQuestionAdminPaginationResponse pagination;

    private List<KyzzQuestionBankOptionResponse> questionBanks;

    private List<KyzzCategoryOptionResponse> categories;
}
