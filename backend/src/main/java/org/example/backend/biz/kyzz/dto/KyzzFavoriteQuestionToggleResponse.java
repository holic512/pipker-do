package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 索引: KYZZ 用户侧题目收藏状态响应。
 */
@Data
@AllArgsConstructor
public class KyzzFavoriteQuestionToggleResponse implements Serializable {

    private Long questionId;

    private Boolean isFavorite;
}
