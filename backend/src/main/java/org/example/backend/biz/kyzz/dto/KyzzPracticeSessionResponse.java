package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI 索引: KYZZ 用户侧刷题会话。
 */
@Data
@AllArgsConstructor
public class KyzzPracticeSessionResponse implements Serializable {

    private KyzzPracticeBankRecordResponse activeBank;

    private List<KyzzPracticeBankRecordResponse> switchableBanks;

    private KyzzPracticeSessionProgressResponse progress;

    private KyzzPracticeQuestionResponse question;

    private Long previousQuestionId;

    private Integer previousQuestionIndex;

    private KyzzPracticeReviewResponse reviewResult;
}
