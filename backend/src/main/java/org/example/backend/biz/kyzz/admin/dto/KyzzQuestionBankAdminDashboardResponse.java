package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class KyzzQuestionBankAdminDashboardResponse {

    private KyzzQuestionBankAdminStatsResponse stats;

    private List<KyzzQuestionBankAdminItemResponse> banks;

    private List<KyzzCategoryOptionResponse> categories;
}
