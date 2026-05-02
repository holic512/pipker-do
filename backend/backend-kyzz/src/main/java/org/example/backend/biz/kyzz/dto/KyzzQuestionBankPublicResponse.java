package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI 索引: KYZZ 用户侧公共题库列表响应。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionBankPublicResponse implements Serializable {

    private KyzzQuestionBankPublicSummaryResponse summary;

    private List<KyzzQuestionBankPublicCategoryResponse> categories;

    private List<KyzzQuestionBankPublicRecordResponse> records;
}
