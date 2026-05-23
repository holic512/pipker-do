/**
 * @file KyyyFavoriteWordResponse
 * @project pipker-do
 * @module 考研英语 / 单词收藏
 * @description 聚合单词收藏列表总数和明细记录，作为收藏页单次加载响应。
 * @logic 1. 输出收藏总数；2. 输出关键词筛选后的记录；3. 保持接口结构轻量稳定。
 * @dependencies DTO: KyyyFavoriteWordRecordResponse
 * @index_tags 考研英语, 单词收藏, 列表响应, 用户侧接口
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class KyyyFavoriteWordResponse implements Serializable {

    private Integer totalCount;

    private List<KyyyFavoriteWordRecordResponse> records;
}
