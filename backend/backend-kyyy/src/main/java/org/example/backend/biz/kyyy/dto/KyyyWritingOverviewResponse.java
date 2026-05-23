/**
 * @file KyyyWritingOverviewResponse
 * @project pipker-do
 * @module 考研英语 / 作文知识库
 * @description 定义作文知识库首页总览响应，聚合方向、分区、题型、年份与推荐真题。
 * @logic 1. 返回英一英二计数；2. 返回大小作文计数；3. 返回题型分面、年份分面与推荐记录。
 * @dependencies API: /api/kyyy/writing/overview
 * @index_tags 考研英语, 作文首页, 总览, 分面, 知识库
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class KyyyWritingOverviewResponse implements Serializable {

    private List<KyyyWritingFacetResponse> examDirections;

    private List<KyyyWritingFacetResponse> essaySections;

    private List<KyyyWritingFacetResponse> promptCategories;

    private List<Integer> recentYears;

    private List<KyyyWritingEssayCardResponse> featuredRecords;
}
