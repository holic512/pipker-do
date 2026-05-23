/**
 * @file KyyyWritingFacetResponse
 * @project pipker-do
 * @module 考研英语 / 作文知识库
 * @description 定义作文知识库筛选分面项，统一承载考试方向、作文分区与题型计数。
 * @logic 1. 提供分面编码与文案；2. 返回计数结果；3. 在题型分面中带出所属作文分区。
 * @dependencies API: /api/kyyy/writing/overview
 * @index_tags 考研英语, 作文, 分面, 题型, 知识库
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class KyyyWritingFacetResponse implements Serializable {

    private String code;

    private String label;

    private Long count;

    private String essaySection;
}
