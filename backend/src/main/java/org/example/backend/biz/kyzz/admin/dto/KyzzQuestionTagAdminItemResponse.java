package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 索引: KYZZ 题目标签管理列表项。
 */
@Data
@AllArgsConstructor
public class KyzzQuestionTagAdminItemResponse {

    private Long id;

    private String tagName;

    private String color;

    private Integer useCount;

    private Integer actualUseCount;

    private Boolean canDelete;

    private String deleteBlockReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
