package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 索引: KYZZ 用户侧排行榜摘要响应。
 */
@Data
@AllArgsConstructor
public class KyzzLeaderboardSummaryResponse implements Serializable {

    private String scope;

    private String periodLabel;

    private LocalDateTime periodStart;

    private LocalDateTime periodEnd;

    private Integer participantCount;

    private LocalDateTime generatedAt;

    private String ruleDescription;
}
