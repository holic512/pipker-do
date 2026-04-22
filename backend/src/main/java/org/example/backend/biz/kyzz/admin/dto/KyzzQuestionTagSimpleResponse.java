package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 索引: KYZZ 题目标签简项。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionTagSimpleResponse {

    private Long id;

    private String tagName;

    private String color;
}
