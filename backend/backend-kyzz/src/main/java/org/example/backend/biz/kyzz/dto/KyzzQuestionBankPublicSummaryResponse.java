package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 索引: KYZZ 用户侧公共题库摘要。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionBankPublicSummaryResponse implements Serializable {

    private Integer totalCount;

    private Integer selectedCount;

    private Integer unselectedCount;
}
