package org.example.backend.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * AI 索引: 后台通用分页响应。
 */
@Data
@AllArgsConstructor
public class PageResponse<T> {

    private Long pageNo;

    private Long pageSize;

    private Long total;

    private Long totalPages;

    private List<T> records;
}
