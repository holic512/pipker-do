package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI 索引: KYZZ 用户侧排行榜响应。
 */
@Data
@AllArgsConstructor
public class KyzzLeaderboardResponse implements Serializable {

    private KyzzLeaderboardSummaryResponse summary;

    private KyzzLeaderboardRecordResponse myRecord;

    private List<KyzzLeaderboardRecordResponse> records;
}
