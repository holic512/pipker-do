package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 索引: KYYY 首页学习与复习数量响应。
 */
@Data
@AllArgsConstructor
public class KyyyHomeDashboardResponse implements Serializable {

    private Integer studyCount;

    private Integer reviewCount;
}
