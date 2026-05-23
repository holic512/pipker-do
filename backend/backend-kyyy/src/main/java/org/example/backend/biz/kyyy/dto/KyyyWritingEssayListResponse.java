/**
 * @file KyyyWritingEssayListResponse
 * @project pipker-do
 * @module 考研英语 / 作文知识库
 * @description 定义作文知识库分页列表响应。
 * @logic 1. 返回作文卡片记录；2. 返回当前分页参数；3. 返回是否还有下一页与总量。
 * @dependencies API: /api/kyyy/writing/essays
 * @index_tags 考研英语, 作文列表, 分页, 知识库, 真题
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class KyyyWritingEssayListResponse implements Serializable {

    private List<KyyyWritingEssayCardResponse> records;

    private Long pageNo;

    private Long pageSize;

    private Boolean hasMore;

    private Long total;
}
