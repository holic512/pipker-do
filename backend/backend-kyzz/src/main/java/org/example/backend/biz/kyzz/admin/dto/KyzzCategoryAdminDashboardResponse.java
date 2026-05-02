package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class KyzzCategoryAdminDashboardResponse {

    private KyzzCategoryAdminStatsResponse stats;

    private List<KyzzCategoryAdminItemResponse> categories;
}
