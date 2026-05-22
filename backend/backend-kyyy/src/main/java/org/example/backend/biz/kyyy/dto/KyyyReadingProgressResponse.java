/**
 * @file KyyyReadingProgressResponse
 * @project pipker-do
 * @module 考研英语 / 阅读做题
 * @description 返回阅读会话当前进度统计。
 * @logic 1. 输出总题数；2. 输出已答和未答数量；3. 标记是否已经提交。
 * @dependencies DTO: KyyyReadingSessionResponse
 * @index_tags 考研英语, 阅读进度, 会话进度, 统计响应
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class KyyyReadingProgressResponse implements Serializable {

    private Integer totalQuestions;

    private Integer answeredCount;

    private Integer unansweredCount;

    private Boolean submitted;
}
