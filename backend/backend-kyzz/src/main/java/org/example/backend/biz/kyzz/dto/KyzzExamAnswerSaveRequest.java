package org.example.backend.biz.kyzz.dto;

import lombok.Data;

import java.util.List;

/**
 * AI 索引: KYZZ VIP 考试答案暂存请求。
 */
@Data
public class KyzzExamAnswerSaveRequest {

    private List<String> selectedOptionKeys;

    private String answerText;

    private Integer usedSeconds;
}
