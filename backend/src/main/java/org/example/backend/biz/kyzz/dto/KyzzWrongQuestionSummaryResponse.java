package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 索引: KYZZ 用户侧错题本摘要。
 */
@Data
@AllArgsConstructor
public class KyzzWrongQuestionSummaryResponse {

    private Integer totalCount;

    private Integer activeCount;

    private Integer masteredCount;

    private Integer totalWrongTimes;
}
