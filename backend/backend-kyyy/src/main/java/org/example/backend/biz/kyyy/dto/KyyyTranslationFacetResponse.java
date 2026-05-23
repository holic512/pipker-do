/**
 * @file KyyyTranslationFacetResponse
 * @project pipker-do
 * @module 考研英语 / 翻译知识库
 * @description 定义翻译知识库筛选分面项，统一承载方向或模式计数。
 * @logic 1. 提供分面编码与文案；2. 返回计数结果；3. 供首页总览复用。
 * @dependencies API: /api/kyyy/translation/overview
 * @index_tags 考研英语, 翻译, 分面, 知识库, 总览
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class KyyyTranslationFacetResponse implements Serializable {

    private String code;

    private String label;

    private Long count;
}
