package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 索引: KYZZ VIP 考试答案暂存结果。
 */
@Data
@AllArgsConstructor
public class KyzzExamAnswerSaveResponse implements Serializable {

    private Long sessionId;

    private Long questionId;

    private Integer answeredCount;

    private Integer totalQuestionCount;
}
