package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 索引: KYZZ 管理端用户题库选择更新结果。
 */
@Data
@AllArgsConstructor
public class KyzzUserQuestionBankSelectionUpdateResponse {

    private Boolean selected;

    private KyzzUserQuestionBankAdminSelectedBankResponse record;
}
