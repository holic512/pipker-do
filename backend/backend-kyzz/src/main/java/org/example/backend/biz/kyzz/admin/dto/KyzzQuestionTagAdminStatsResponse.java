package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 索引: KYZZ 题目标签管理统计。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionTagAdminStatsResponse {

    private Integer totalTags;

    private Integer usedTags;

    private Integer unusedTags;

    private Integer taggedQuestionCount;
}
