package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 索引: KYZZ 题目管理概览统计。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionAdminStatsResponse {

    private Integer totalQuestions;

    private Integer activeQuestions;

    private Integer inactiveQuestions;

    private Integer singleChoiceQuestions;

    private Integer multipleChoiceQuestions;

    private Integer shortAnswerQuestions;
}
