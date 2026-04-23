package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 索引: KYZZ 管理端用户题库选择统计。
 */
@Data
@AllArgsConstructor
public class KyzzUserQuestionBankAdminStatsResponse {

    private Long totalUsers;

    private Long selectedUsers;

    private Long unselectedUsers;

    private Long totalSelections;
}
