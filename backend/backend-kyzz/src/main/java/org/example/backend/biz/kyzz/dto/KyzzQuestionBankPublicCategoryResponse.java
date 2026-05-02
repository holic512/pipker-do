package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 索引: KYZZ 用户侧公共题库分类筛选项。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionBankPublicCategoryResponse implements Serializable {

    private Long id;

    private String categoryName;

    private Integer categoryLevel;
}
