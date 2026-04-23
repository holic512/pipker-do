package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 索引: KYZZ 排行榜实时聚合记录。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KyzzLeaderboardAggregateRecord implements Serializable {

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
