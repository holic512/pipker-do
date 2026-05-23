/**
 * @file KyyyLeaderboardSummaryResponse
 * @project pipker-do
 * @module 考研英语 / 排行榜
 * @description 定义英语排行榜顶部摘要区所需的周期和规则信息。
 * @logic 1. 返回榜单周期与时间范围；2. 返回参与人数与生成时间；3. 返回综合榜排序规则说明。
 * @dependencies API: /api/kyyy/leaderboard
 * @index_tags 考研英语, 排行榜, 摘要信息, 周期说明
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class KyyyLeaderboardSummaryResponse implements Serializable {

    private String scope;

    private String periodLabel;

    private LocalDateTime periodStart;

    private LocalDateTime periodEnd;

    private Integer participantCount;

    private LocalDateTime generatedAt;

    private String ruleDescription;
}
