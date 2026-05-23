/**
 * @file KyyyTranslationSegmentResponse
 * @project pipker-do
 * @module 考研英语 / 翻译知识库
 * @description 定义翻译题详情中的分段结构。
 * @logic 1. 返回分段题号；2. 返回英文原文与中文译文；3. 支持英一逐句和英二整段展示。
 * @dependencies API: /api/kyyy/translation/passages/{passageId}
 * @index_tags 考研英语, 翻译分段, 详情, 真题, 知识库
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class KyyyTranslationSegmentResponse implements Serializable {

    private Integer segmentNo;

    private String sourceText;

    private String translatedText;
}
