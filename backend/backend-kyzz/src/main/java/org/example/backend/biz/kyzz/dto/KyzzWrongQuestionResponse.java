package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI 索引: KYZZ 用户侧错题本响应。
 */
@Data
@AllArgsConstructor
public class KyzzWrongQuestionResponse implements Serializable {

    private KyzzWrongQuestionSummaryResponse summary;

    private List<KyzzWrongQuestionRecordResponse> records;
}
