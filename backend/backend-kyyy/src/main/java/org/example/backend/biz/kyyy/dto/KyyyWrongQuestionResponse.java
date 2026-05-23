/**
 * @file KyyyWrongQuestionResponse
 * @project pipker-do
 * @module 考研英语 / 阅读错题本
 * @description 聚合阅读错题本汇总与明细列表，作为小程序错题本单次加载响应。
 * @logic 1. 输出顶部统计信息；2. 输出当前筛选后的错题记录；3. 保持接口结构稳定便于前端归一化。
 * @dependencies DTO: KyyyWrongQuestionSummaryResponse, DTO: KyyyWrongQuestionRecordResponse
 * @index_tags 考研英语, 阅读错题本, 列表响应, 用户侧接口
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class KyyyWrongQuestionResponse implements Serializable {

    private KyyyWrongQuestionSummaryResponse summary;

    private List<KyyyWrongQuestionRecordResponse> records;
}
