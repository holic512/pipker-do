package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 索引: KYZZ 用户侧刷题进度重置结果。
 */
@Data
@AllArgsConstructor
public class KyzzPracticeProgressResetResponse implements Serializable {

    private Integer deletedAnswerCount;

    private Integer deletedWrongQuestionCount;

    private Integer resetQuestionBankCount;
}
