/**
 * @file KyyyWordExampleResponse
 * @project pipker-do
 * @module 考研英语 / 词库例句
 * @description 向小程序返回单词多例句展示数据。
 * @logic 1. 暴露例句 ID；2. 暴露英文例句与中文翻译；3. 保持与旧 exampleSentence 字段并行兼容。
 * @dependencies Entity: KyyyWordExample
 * @index_tags 考研英语, 单词例句, 多例句, 响应DTO
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class KyyyWordExampleResponse implements Serializable {

    private Long id;

    private String exampleSentence;

    private String exampleTranslation;
}
