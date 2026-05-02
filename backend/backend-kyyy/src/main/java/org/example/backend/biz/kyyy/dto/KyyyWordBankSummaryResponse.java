package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 索引: KYYY 用户侧词库列表摘要。
 */
@Data
@AllArgsConstructor
public class KyyyWordBankSummaryResponse implements Serializable {

    private Integer totalCount;

    private Integer selectedCount;
}
