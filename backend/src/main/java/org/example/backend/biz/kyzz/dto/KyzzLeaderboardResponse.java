package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * AI 索引: KYZZ 用户侧排行榜响应。
 */
@Data
@AllArgsConstructor
public class KyzzLeaderboardResponse {

    private KyzzLeaderboardSummaryResponse summary;

    private KyzzLeaderboardRecordResponse myRecord;

    private List<KyzzLeaderboardRecordResponse> records;
}
