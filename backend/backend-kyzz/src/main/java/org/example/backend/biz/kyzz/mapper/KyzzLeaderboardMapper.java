package org.example.backend.biz.kyzz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.backend.biz.kyzz.dto.KyzzLeaderboardAggregateRecord;
import org.example.backend.biz.kyzz.entity.KyzzLeaderboard;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface KyzzLeaderboardMapper extends BaseMapper<KyzzLeaderboard> {

    @Select("""
            <script>
            SELECT
                ranked.rank_no AS rankNo,
                ranked.user_id AS userId,
                ranked.nickname AS nickname,
                ranked.avatar_url AS avatarUrl,
                ranked.study_count AS studyCount,
                ranked.correct_count AS correctCount,
                ranked.accuracy_rate AS accuracyRate,
                ranked.last_practice_at AS lastPracticeAt
            FROM (
                SELECT
                    agg.user_id,
                    agg.nickname,
                    agg.avatar_url,
                    agg.study_count,
                    agg.correct_count,
                    agg.accuracy_rate,
                    agg.last_practice_at,
                    ROW_NUMBER() OVER (
                        ORDER BY
                            agg.study_count DESC,
                            agg.accuracy_rate DESC,
                            agg.last_practice_at DESC,
                            agg.user_id ASC
                    ) AS rank_no
                FROM (
                    SELECT
                        ua.user_id,
                        u.nickname,
                        u.avatar_url,
                        COUNT(*) AS study_count,
                        SUM(CASE WHEN ua.is_correct = 1 THEN 1 ELSE 0 END) AS correct_count,
                        ROUND(SUM(CASE WHEN ua.is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS accuracy_rate,
                        MAX(ua.submitted_at) AS last_practice_at
                    FROM kyzz_user_answer ua
                    INNER JOIN app_user u ON u.id = ua.user_id AND u.status = 1
                    WHERE ua.answer_status = 1
                    <if test='startAt != null'>
                        AND ua.submitted_at <![CDATA[>=]]> #{startAt}
                    </if>
                    <if test='endAt != null'>
                        AND ua.submitted_at <![CDATA[<=]]> #{endAt}
                    </if>
                    GROUP BY ua.user_id, u.nickname, u.avatar_url
                ) agg
            ) ranked
            ORDER BY ranked.rank_no ASC
            LIMIT #{limit}
            </script>
            """)
    List<KyzzLeaderboardAggregateRecord> selectTopAggregateRecords(@Param("startAt") LocalDateTime startAt,
                                                                   @Param("endAt") LocalDateTime endAt,
                                                                   @Param("limit") Integer limit);

    @Select("""
            <script>
            SELECT
                ua.user_id AS userId,
                u.nickname AS nickname,
                u.avatar_url AS avatarUrl,
                COUNT(*) AS studyCount,
                SUM(CASE WHEN ua.is_correct = 1 THEN 1 ELSE 0 END) AS correctCount,
                ROUND(SUM(CASE WHEN ua.is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS accuracyRate,
                MAX(ua.submitted_at) AS lastPracticeAt
            FROM kyzz_user_answer ua
            INNER JOIN app_user u ON u.id = ua.user_id AND u.status = 1
            WHERE ua.answer_status = 1
              AND ua.user_id = #{userId}
            <if test='startAt != null'>
                AND ua.submitted_at <![CDATA[>=]]> #{startAt}
            </if>
            <if test='endAt != null'>
                AND ua.submitted_at <![CDATA[<=]]> #{endAt}
            </if>
            GROUP BY ua.user_id, u.nickname, u.avatar_url
            LIMIT 1
            </script>
            """)
    KyzzLeaderboardAggregateRecord selectUserAggregateRecord(@Param("startAt") LocalDateTime startAt,
                                                             @Param("endAt") LocalDateTime endAt,
                                                             @Param("userId") Long userId);

    @Select("""
            <script>
            SELECT ranked.rank_no
            FROM (
                SELECT
                    agg.user_id,
                    ROW_NUMBER() OVER (
                        ORDER BY
                            agg.study_count DESC,
                            agg.accuracy_rate DESC,
                            agg.last_practice_at DESC,
                            agg.user_id ASC
                    ) AS rank_no
                FROM (
                    SELECT
                        ua.user_id,
                        COUNT(*) AS study_count,
                        ROUND(SUM(CASE WHEN ua.is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS accuracy_rate,
                        MAX(ua.submitted_at) AS last_practice_at
                    FROM kyzz_user_answer ua
                    INNER JOIN app_user u ON u.id = ua.user_id AND u.status = 1
                    WHERE ua.answer_status = 1
                    <if test='startAt != null'>
                        AND ua.submitted_at <![CDATA[>=]]> #{startAt}
                    </if>
                    <if test='endAt != null'>
                        AND ua.submitted_at <![CDATA[<=]]> #{endAt}
                    </if>
                    GROUP BY ua.user_id
                ) agg
            ) ranked
            WHERE ranked.user_id = #{userId}
            LIMIT 1
            </script>
            """)
    Integer selectUserRankNo(@Param("startAt") LocalDateTime startAt,
                             @Param("endAt") LocalDateTime endAt,
                             @Param("userId") Long userId);

    @Select("""
            <script>
            SELECT COUNT(DISTINCT ua.user_id)
            FROM kyzz_user_answer ua
            INNER JOIN app_user u ON u.id = ua.user_id AND u.status = 1
            WHERE ua.answer_status = 1
            <if test='startAt != null'>
                AND ua.submitted_at <![CDATA[>=]]> #{startAt}
            </if>
            <if test='endAt != null'>
                AND ua.submitted_at <![CDATA[<=]]> #{endAt}
            </if>
            </script>
            """)
    Long countParticipants(@Param("startAt") LocalDateTime startAt,
                           @Param("endAt") LocalDateTime endAt);
}
