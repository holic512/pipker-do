package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * AI 索引: KYZZ 题目标签管理首页数据。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionTagAdminDashboardResponse {

    private KyzzQuestionTagAdminStatsResponse stats;

    private List<KyzzQuestionTagAdminItemResponse> tags;
}
