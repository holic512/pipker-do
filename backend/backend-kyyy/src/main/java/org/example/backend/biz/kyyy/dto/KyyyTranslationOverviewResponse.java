/**
 * @file KyyyTranslationOverviewResponse
 * @project pipker-do
 * @module 考研英语 / 翻译知识库
 * @description 定义翻译知识库首页总览响应，聚合方向、模式、年份与推荐真题。
 * @logic 1. 返回英一英二计数；2. 返回模式计数；3. 返回年份分面与推荐记录。
 * @dependencies API: /api/kyyy/translation/overview
 * @index_tags 考研英语, 翻译首页, 总览, 分面, 知识库
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class KyyyTranslationOverviewResponse implements Serializable {

    private List<KyyyTranslationFacetResponse> examDirections;

    private List<KyyyTranslationFacetResponse> translationModes;

    private List<Integer> recentYears;

    private List<KyyyTranslationCardResponse> featuredRecords;
}
