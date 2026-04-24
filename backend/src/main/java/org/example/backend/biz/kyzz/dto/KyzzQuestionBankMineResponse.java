package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI 索引: KYZZ 用户侧我的题库列表响应。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionBankMineResponse implements Serializable {

    private KyzzQuestionBankMineSummaryResponse summary;

    private List<KyzzQuestionBankMineRecordResponse> records;
}
