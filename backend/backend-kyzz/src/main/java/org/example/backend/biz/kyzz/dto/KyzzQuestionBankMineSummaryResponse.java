package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 索引: KYZZ 用户侧我的题库摘要。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionBankMineSummaryResponse implements Serializable {

    private Integer selectedCount;

    private Integer inProgressCount;

    private Integer completedCount;
}
