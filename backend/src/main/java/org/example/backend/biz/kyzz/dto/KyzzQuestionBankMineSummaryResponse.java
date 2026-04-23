package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 索引: KYZZ 用户侧我的题库摘要。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionBankMineSummaryResponse {

    private Integer selectedCount;

    private Integer inProgressCount;

    private Integer completedCount;
}
