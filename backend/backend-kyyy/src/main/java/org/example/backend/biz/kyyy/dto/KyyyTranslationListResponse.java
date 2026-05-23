/**
 * @file KyyyTranslationListResponse
 * @project pipker-do
 * @module 考研英语 / 翻译知识库
 * @description 定义翻译知识库分页列表响应。
 * @logic 1. 返回翻译卡片记录；2. 返回分页参数；3. 返回是否还有下一页与总量。
 * @dependencies API: /api/kyyy/translation/passages
 * @index_tags 考研英语, 翻译列表, 分页, 知识库, 真题
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class KyyyTranslationListResponse implements Serializable {

    private List<KyyyTranslationCardResponse> records;

    private Long pageNo;

    private Long pageSize;

    private Boolean hasMore;

    private Long total;
}
