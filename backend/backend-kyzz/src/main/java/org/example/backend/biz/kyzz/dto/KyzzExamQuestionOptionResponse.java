package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 索引: KYZZ VIP 考试题目选项。
 */
@Data
@AllArgsConstructor
public class KyzzExamQuestionOptionResponse implements Serializable {

    private String optionKey;

    private String optionContent;
}
