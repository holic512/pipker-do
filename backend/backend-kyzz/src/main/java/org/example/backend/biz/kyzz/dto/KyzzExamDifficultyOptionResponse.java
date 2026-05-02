package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 索引: KYZZ VIP 考试难度选项。
 */
@Data
@AllArgsConstructor
public class KyzzExamDifficultyOptionResponse implements Serializable {

    private String difficultyMode;

    private String title;

    private String description;
}
