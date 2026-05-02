package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI 索引: KYYY 用户侧词库列表响应。
 */
@Data
@AllArgsConstructor
public class KyyyWordBankListResponse implements Serializable {

    private KyyyWordBankSummaryResponse summary;

    private List<KyyyWordBankRecordResponse> records;
}
