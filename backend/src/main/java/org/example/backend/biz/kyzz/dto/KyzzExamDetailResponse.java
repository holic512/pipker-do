package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI 索引: KYZZ VIP 考试详情。
 */
@Data
@AllArgsConstructor
public class KyzzExamDetailResponse implements Serializable {

    private KyzzExamSummaryResponse summary;

    private List<KyzzExamQuestionResponse> questions;

    private Boolean canAnswer;

    private Boolean canSubmit;
}
