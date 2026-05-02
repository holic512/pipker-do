package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 索引: KYZZ 管理端用户题库选择详情摘要。
 */
@Data
@AllArgsConstructor
public class KyzzUserQuestionBankAdminSummaryResponse {

    private Integer selectedBankCount;

    private Integer inProgressBankCount;

    private Integer completedBankCount;
}
