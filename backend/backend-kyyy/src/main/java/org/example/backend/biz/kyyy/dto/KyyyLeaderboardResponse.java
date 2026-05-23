/**
 * @file KyyyLeaderboardResponse
 * @project pipker-do
 * @module 考研英语 / 排行榜
 * @description 定义英语用户侧排行榜接口的完整响应结构。
 * @logic 1. 返回榜单摘要；2. 返回当前用户个人记录；3. 返回当前周期榜单列表。
 * @dependencies API: /api/kyyy/leaderboard
 * @index_tags 考研英语, 排行榜, 响应结构, 用户侧接口
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class KyyyLeaderboardResponse implements Serializable {

    private KyyyLeaderboardSummaryResponse summary;

    private KyyyLeaderboardRecordResponse myRecord;

    private List<KyyyLeaderboardRecordResponse> records;
}
