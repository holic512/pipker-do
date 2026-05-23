/**
 * @file KyyyWrongQuestionSummaryResponse
 * @project pipker-do
 * @module 考研英语 / 阅读错题本
 * @description 返回阅读错题本汇总统计，支撑顶部状态筛选和累计数据展示。
 * @logic 1. 统计全部错题数；2. 区分待巩固与已掌握数量；3. 汇总累计答错次数。
 * @dependencies API: /api/kyyy/wrong-questions
 * @index_tags 考研英语, 阅读错题本, 汇总统计, 状态计数
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class KyyyWrongQuestionSummaryResponse implements Serializable {

    private Integer totalCount;

    private Integer activeCount;

    private Integer masteredCount;

    private Integer totalWrongTimes;
}
