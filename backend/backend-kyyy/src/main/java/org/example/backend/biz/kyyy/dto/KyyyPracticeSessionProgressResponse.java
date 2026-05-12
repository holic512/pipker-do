/**
 * @file KyyyPracticeSessionProgressResponse
 * @project pipker-do
 * @module 考研英语 / 背词会话
 * @description 返回会话内的顶部进度和评分统计。
 * @logic 1. 返回预算与已完成数量；2. 返回剩余卡片与当前序号；3. 返回认识、模糊、不认识计数。
 * @dependencies DTO: KyyyPracticeSessionResponse
 * @index_tags 考研英语, 会话进度, 学习统计, 响应DTO
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class KyyyPracticeSessionProgressResponse implements Serializable {

    private Integer totalCards;

    private Integer completedCards;

    private Integer remainingCards;

    private Integer currentIndex;

    private Integer knownCount;

    private Integer fuzzyCount;

    private Integer unknownCount;
}
