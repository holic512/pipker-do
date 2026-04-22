package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KyzzQuestionBankAdminStatsResponse {

    private Integer totalBanks;

    private Integer activeBanks;

    private Integer inactiveBanks;

    private Integer uncategorizedBanks;

    private Integer totalQuestions;

    private Integer totalStudyUsers;
}
