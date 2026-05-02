package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 索引: KYYY 抽词来源词库响应。
 */
@Data
@AllArgsConstructor
public class KyyyWordSourceBankResponse implements Serializable {

    private Long id;

    private String bankCode;

    private String bankName;
}
