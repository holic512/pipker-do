package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI 索引: KYZZ 用户侧收藏题目列表响应。
 */
@Data
@AllArgsConstructor
public class KyzzFavoriteQuestionResponse implements Serializable {

    private Integer totalCount;

    private List<KyzzFavoriteQuestionRecordResponse> records;
}
