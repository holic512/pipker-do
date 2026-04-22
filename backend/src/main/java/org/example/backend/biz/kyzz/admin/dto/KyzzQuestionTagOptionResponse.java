package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 索引: KYZZ 题目标签选项。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionTagOptionResponse {

    private Long id;

    private String tagName;

    private String color;

    private Integer useCount;
}
