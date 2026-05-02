package org.example.backend.biz.kyzz.dto;

import lombok.Data;

/**
 * AI 索引: KYZZ VIP 考试创建请求。
 */
@Data
public class KyzzExamStartRequest {

    private String examType;

    private String difficultyMode;

    private Integer durationMinutes;
}
