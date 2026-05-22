/**
 * @file KyyyReadingQuestionResponse
 * @project pipker-do
 * @module 考研英语 / 阅读做题
 * @description 返回阅读小题、选项和用户当前作答状态。
 * @logic 1. 输出题号与题干；2. 输出全部选项；3. 按会话状态输出用户答案、对错、标准答案与解析。
 * @dependencies DTO: KyyyReadingOptionResponse
 * @index_tags 考研英语, 阅读题目, 作答结果, 整篇做题
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class KyyyReadingQuestionResponse implements Serializable {

    private Long questionId;

    private Integer questionNo;

    private String stem;

    private List<KyyyReadingOptionResponse> options;

    private String selectedOptionKey;

    private String answerStatus;

    private Boolean isCorrect;

    private String correctAnswer;

    private String analysis;

    private List<KyyyReadingAnnotationResponse> annotations;
}
