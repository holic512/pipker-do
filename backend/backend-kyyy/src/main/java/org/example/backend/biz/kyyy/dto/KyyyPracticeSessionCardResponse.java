/**
 * @file KyyyPracticeSessionCardResponse
 * @project pipker-do
 * @module 考研英语 / 背词会话
 * @description 返回当前待学习单词卡片及其记忆上下文。
 * @logic 1. 暴露单词正文、音标、释义与例句；2. 暴露来源、轮次与顺序；3. 暴露长期进度与关联词。
 * @dependencies Entity: KyyyWord, DTO: KyyyRelatedWordResponse, DTO: KyyyWordSourceBankResponse
 * @index_tags 考研英语, 单词卡片, 学习卡片, 会话当前题
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class KyyyPracticeSessionCardResponse implements Serializable {

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

    private Integer memoryStage;

    private Integer learningStep;

    private Integer reviewCount;

    private Integer correctCount;

    private Integer wrongCount;

    private String lastResult;

    private LocalDateTime lastStudiedAt;

    private LocalDateTime nextReviewAt;

    private String sourceType;

    private Integer roundNo;

    private Integer queueOrder;

    private List<KyyyWordSourceBankResponse> sourceBanks;

    private List<KyyyRelatedWordResponse> relatedWords;
}
