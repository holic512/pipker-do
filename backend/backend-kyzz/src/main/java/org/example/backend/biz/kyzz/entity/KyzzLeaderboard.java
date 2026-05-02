package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考研政治排行榜实体。
 */
@Data
@TableName("kyzz_leaderboard")
public class KyzzLeaderboard implements Serializable {

    /** 排行榜记录主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 排行榜类型。 */
    private String leaderboardType;

    /** 统计日期。 */
    private LocalDate statDate;

    /** 用户 ID。 */
    private Long userId;

    /** 学习题目数量。 */
    private Integer studyCount;

    /** 正确题目数量。 */
    private Integer correctCount;

    /** 正确率。 */
    private BigDecimal accuracyRate;

    /** 排行分值。 */
    private BigDecimal scoreValue;

    /** 排名序号。 */
    private Integer rankNo;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
