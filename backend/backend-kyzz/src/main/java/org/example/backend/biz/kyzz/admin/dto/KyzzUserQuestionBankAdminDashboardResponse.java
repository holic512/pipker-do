package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * AI 索引: KYZZ 管理端用户题库选择首页数据。
 */
@Data
@AllArgsConstructor
public class KyzzUserQuestionBankAdminDashboardResponse {

    private KyzzUserQuestionBankAdminStatsResponse stats;

    private List<KyzzUserQuestionBankAdminUserItemResponse> records;

    private KyzzUserQuestionBankAdminPaginationResponse pagination;
}
