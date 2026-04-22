package org.example.backend.biz.kyzz.admin.dto;

import lombok.Data;

/**
 * AI 索引: KYZZ 题目标签新增编辑入参。
 */
@Data
public class KyzzQuestionTagAdminUpsertRequest {

    private String tagName;

    private String color;
}
