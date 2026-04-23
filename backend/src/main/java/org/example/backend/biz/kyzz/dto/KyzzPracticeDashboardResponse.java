package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * AI 索引: KYZZ 用户侧刷题看板。
 */
@Data
@AllArgsConstructor
public class KyzzPracticeDashboardResponse {

    private Long recommendedBankId;

    private String recommendedReason;

    private List<KyzzPracticeBankRecordResponse> records;
}
