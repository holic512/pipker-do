/**
 * @file KyyyLeaderboardRecordResponse
 * @project pipker-do
 * @module 考研英语 / 排行榜
 * @description 定义英语用户侧排行榜中的单条用户展示记录。
 * @logic 1. 返回名次与用户基础信息；2. 返回综合学习量、正确数和正确率；3. 标记当前记录是否属于当前用户。
 * @dependencies API: /api/kyyy/leaderboard
 * @index_tags 考研英语, 排行榜, 用户记录, API响应
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class KyyyLeaderboardRecordResponse implements Serializable {

    private Integer rankNo;

    private Long userId;

    private String nickname;

    private String avatarUrl;

    private Integer studyCount;

    private Integer correctCount;

    private BigDecimal accuracyRate;

    private LocalDateTime lastPracticeAt;

    private Boolean isMe;
}
