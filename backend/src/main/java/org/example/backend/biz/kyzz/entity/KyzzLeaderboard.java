package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("kyzz_leaderboard")
public class KyzzLeaderboard implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String leaderboardType;

    private LocalDate statDate;

    private Long userId;

    private Integer studyCount;

    private Integer correctCount;

    private BigDecimal accuracyRate;

    private BigDecimal scoreValue;

    private Integer rankNo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
