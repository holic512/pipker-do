package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI 索引: KYZZ 用户侧刷题答案预览，只读返回标准答案与解析。
 */
@Data
@AllArgsConstructor
public class KyzzPracticeAnswerPreviewResponse implements Serializable {

    private Long questionId;

    private Long bankId;

    private String questionType;

    private List<String> correctOptionKeys;

    private String answerText;

    private String analysis;
}
