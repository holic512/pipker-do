/**
 * @file KyyyPracticeSessionCompletionResponse
 * @project pipker-do
 * @module 考研英语 / 背词会话
 * @description 返回一次会话完成后的结果面板数据。
 * @logic 1. 返回本轮通过的新词数；2. 返回需要尽快回看的词数；3. 返回不认识次数与建议动作。
 * @dependencies DTO: KyyyPracticeSessionResponse
 * @index_tags 考研英语, 完成结果, 会话总结, 复习建议
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class KyyyPracticeSessionCompletionResponse implements Serializable {

    private Integer passedNewCount;

    private Integer dueSoonCount;

    private Integer unknownCount;

    private String primaryActionMode;
}
