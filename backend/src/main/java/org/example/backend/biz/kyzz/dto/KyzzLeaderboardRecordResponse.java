package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 索引: KYZZ 用户侧排行榜记录响应。
 */
@Data
@AllArgsConstructor
public class KyzzLeaderboardRecordResponse implements Serializable {

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
