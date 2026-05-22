/**
 * @file KyyyReadingOptionResponse
 * @project pipker-do
 * @module 考研英语 / 阅读做题
 * @description 返回阅读单题选项信息，供小程序渲染整篇作答页。
 * @logic 1. 输出选项键；2. 输出选项文案；3. 复用于历史快照与会话详情展示。
 * @dependencies DTO: KyyyReadingQuestionResponse
 * @index_tags 考研英语, 阅读选项, 题目选项, 会话响应
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class KyyyReadingOptionResponse implements Serializable {

    private String optionKey;

    private String optionContent;
}
