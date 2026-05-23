/**
 * @file KyyyTranslationCardResponse
 * @project pipker-do
 * @module 考研英语 / 翻译知识库
 * @description 定义翻译知识库列表卡片返回结构，供首页推荐与分页列表复用。
 * @logic 1. 返回翻译题基础信息；2. 返回模式、分值与分段数量；3. 返回标签数组供前端展示。
 * @dependencies API: /api/kyyy/translation/overview, API: /api/kyyy/translation/passages
 * @index_tags 考研英语, 翻译卡片, 列表, 真题, 知识库
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class KyyyTranslationCardResponse implements Serializable {

    private Long id;

    private String translationCode;

    private String sourceTitle;

    private String examDirection;

    private Integer sourceYear;

    private String translationMode;

    private Integer scoreValue;

    private Integer segmentCount;

    private List<String> knowledgeTags;
}
