package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 索引: KYYY 用户侧下一个学习单词响应。
 */
@Data
@AllArgsConstructor
public class KyyyPracticeNextWordResponse implements Serializable {

    private Long wordId;

    private String wordText;

    private String phoneticUs;

    private String phoneticUk;

    private String partOfSpeech;

    private String meaningCn;

    private String exampleSentence;

    private String exampleTranslation;

    private Integer difficultyLevel;

    private String studyStatus;

    private Integer masteryLevel;

    private Integer reviewCount;

    private Integer correctCount;

    private Integer wrongCount;

    private String lastResult;

    private LocalDateTime lastStudiedAt;

    private LocalDateTime nextReviewAt;

    private List<KyyyWordSourceBankResponse> sourceBanks;

    private List<KyyyRelatedWordResponse> relatedWords;
}
