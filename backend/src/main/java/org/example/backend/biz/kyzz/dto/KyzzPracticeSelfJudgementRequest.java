package org.example.backend.biz.kyzz.dto;

import lombok.Data;

import java.util.List;

/**
 * AI 索引: KYZZ 用户侧刷题简答自判请求。
 */
@Data
public class KyzzPracticeSelfJudgementRequest {

    private Long bankId;

    private List<String> selectedOptionKeys;

    private String answerText;

    private Integer usedSeconds;

    private Boolean selfJudgedCorrect;
}
