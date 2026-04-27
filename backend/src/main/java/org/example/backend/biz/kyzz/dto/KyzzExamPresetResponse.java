package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 索引: KYZZ VIP 考试类型预设。
 */
@Data
@AllArgsConstructor
public class KyzzExamPresetResponse implements Serializable {

    private String examType;

    private String title;

    private String description;

    private Integer defaultDurationMinutes;

    private Integer singleCount;

    private Integer multipleCount;

    private Integer shortCount;
}
