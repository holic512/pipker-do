package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KyzzCategoryAdminStatsResponse {

    private Integer totalCategories;

    private Integer enabledCategories;

    private Integer levelOneCategories;

    private Integer linkedQuestionBanks;

    private Integer linkedQuestions;
}
