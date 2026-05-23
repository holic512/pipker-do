/**
 * @file KyyyLeaderboardAggregateRecord
 * @project pipker-do
 * @module 考研英语 / 排行榜
 * @description 承载英语综合排行榜聚合后的单个用户统计记录。
 * @logic 1. 汇总背词与阅读的学习量和正确数；2. 保存综合正确率与最近活跃时间；3. 作为榜单排序与响应转换的中间模型。
 * @dependencies Mapper: KyyyLeaderboardMapper
 * @index_tags 考研英语, 排行榜, 聚合记录, 综合学习榜
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KyyyLeaderboardAggregateRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer rankNo;

    private Long userId;

    private String nickname;

    private String avatarUrl;

    private Integer studyCount;

    private Integer correctCount;

    private BigDecimal accuracyRate;

    private LocalDateTime lastPracticeAt;
}
