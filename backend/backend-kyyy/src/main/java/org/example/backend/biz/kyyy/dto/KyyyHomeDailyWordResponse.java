package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * AI 索引: KYYY 首页每日一词响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KyyyHomeDailyWordResponse implements Serializable {

    private Long wordId;

    private String wordText;

    private String partOfSpeech;

    private String meaningCn;
}
