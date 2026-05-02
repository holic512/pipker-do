package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 索引: KYYY 单词延伸词响应。
 */
@Data
@AllArgsConstructor
public class KyyyRelatedWordResponse implements Serializable {

    private Long id;

    private Long relatedWordId;

    private String relatedWordText;

    private String meaningCn;

    private String relationType;
}
