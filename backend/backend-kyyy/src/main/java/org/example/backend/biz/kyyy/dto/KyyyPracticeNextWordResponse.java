/**
 * @file KyyyPracticeNextWordResponse
 * @project pipker-do
 * @module 考研英语 / 背词会话
 * @description 返回兼容旧入口的下一个学习单词数据。
 * @logic 1. 复用当前会话卡片字段；2. 保留旧单例句字段；3. 返回多例句和相关词用于详情展示。
 * @dependencies DTO: KyyyWordExampleResponse, DTO: KyyyRelatedWordResponse, DTO: KyyyWordSourceBankResponse
 * @index_tags 考研英语, 下一个单词, 多例句, 背词
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
public class KyyyPracticeNextWordResponse implements Serializable {

    private Long wordId;

    private String wordText;

    private String phoneticUs;

    private String phoneticUk;

    private String partOfSpeech;

    private String meaningCn;

    private String exampleSentence;

    private String exampleTranslation;

    private List<KyyyWordExampleResponse> examples;

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
