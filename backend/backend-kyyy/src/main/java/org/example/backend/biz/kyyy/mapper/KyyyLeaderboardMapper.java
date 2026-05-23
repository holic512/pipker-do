/**
 * @file KyyyLeaderboardMapper
 * @project pipker-do
 * @module 考研英语 / 排行榜
 * @description 提供英语综合排行榜的实时聚合查询，包括榜单、个人名次和参与人数统计。
 * @logic 1. 合并背词完成会话与阅读交卷会话；2. 按用户聚合学习量、正确数和最近活跃时间；3. 使用窗口函数生成稳定名次。
 * @dependencies Table: kyyy_word_practice_session, Table: kyyy_reading_session, Table: app_user
 * @index_tags 考研英语, 排行榜, MyBatis, 聚合SQL
 * @author holic512
 */
package org.example.backend.biz.kyyy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.backend.biz.kyyy.dto.KyyyLeaderboardAggregateRecord;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface KyyyLeaderboardMapper {

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
                        combined.user_id,
                        u.nickname,
                        u.avatar_url,
                        SUM(combined.study_count) AS study_count,
                        SUM(combined.correct_count) AS correct_count,
                        ROUND(SUM(combined.correct_count) * 100.0 / SUM(combined.study_count), 2) AS accuracy_rate,
                        MAX(combined.activity_at) AS last_practice_at
                    FROM (
                        SELECT
                            session_row.user_id,
                            SUM(session_row.completed_cards) AS study_count,
                            SUM(session_row.known_count) AS correct_count,
                            MAX(COALESCE(session_row.last_answered_at, session_row.finished_at)) AS activity_at
                        FROM kyyy_word_practice_session session_row
                        WHERE session_row.status = 'completed'
                          AND session_row.completed_cards > 0
                        <if test='startAt != null'>
                            AND session_row.finished_at <![CDATA[>=]]> #{startAt}
                        </if>
                        <if test='endAt != null'>
                            AND session_row.finished_at <![CDATA[<=]]> #{endAt}
                        </if>
                        GROUP BY session_row.user_id
                        UNION ALL
                        SELECT
                            session_row.user_id,
                            SUM(session_row.answered_count) AS study_count,
                            SUM(session_row.correct_count) AS correct_count,
                            MAX(session_row.submitted_at) AS activity_at
                        FROM kyyy_reading_session session_row
                        WHERE session_row.session_status = 'submitted'
                          AND session_row.answered_count > 0
                        <if test='startAt != null'>
                            AND session_row.submitted_at <![CDATA[>=]]> #{startAt}
                        </if>
                        <if test='endAt != null'>
                            AND session_row.submitted_at <![CDATA[<=]]> #{endAt}
                        </if>
                        GROUP BY session_row.user_id
                    ) combined
                    INNER JOIN app_user u ON u.id = combined.user_id AND u.status = 1
                    GROUP BY combined.user_id, u.nickname, u.avatar_url
                ) agg
            ) ranked
            ORDER BY ranked.rank_no ASC
            LIMIT #{limit}
            </script>
            """)
    List<KyyyLeaderboardAggregateRecord> selectTopAggregateRecords(@Param("startAt") LocalDateTime startAt,
                                                                   @Param("endAt") LocalDateTime endAt,
                                                                   @Param("limit") Integer limit);

    @Select("""
            <script>
            SELECT
                agg.user_id AS userId,
                agg.nickname AS nickname,
                agg.avatar_url AS avatarUrl,
                agg.study_count AS studyCount,
                agg.correct_count AS correctCount,
                agg.accuracy_rate AS accuracyRate,
                agg.last_practice_at AS lastPracticeAt
            FROM (
                SELECT
                    combined.user_id,
                    u.nickname,
                    u.avatar_url,
                    SUM(combined.study_count) AS study_count,
                    SUM(combined.correct_count) AS correct_count,
                    ROUND(SUM(combined.correct_count) * 100.0 / SUM(combined.study_count), 2) AS accuracy_rate,
                    MAX(combined.activity_at) AS last_practice_at
                FROM (
                    SELECT
                        session_row.user_id,
                        SUM(session_row.completed_cards) AS study_count,
                        SUM(session_row.known_count) AS correct_count,
                        MAX(COALESCE(session_row.last_answered_at, session_row.finished_at)) AS activity_at
                    FROM kyyy_word_practice_session session_row
                    WHERE session_row.status = 'completed'
                      AND session_row.completed_cards > 0
                      AND session_row.user_id = #{userId}
                    <if test='startAt != null'>
                        AND session_row.finished_at <![CDATA[>=]]> #{startAt}
                    </if>
                    <if test='endAt != null'>
                        AND session_row.finished_at <![CDATA[<=]]> #{endAt}
                    </if>
                    GROUP BY session_row.user_id
                    UNION ALL
                    SELECT
                        session_row.user_id,
                        SUM(session_row.answered_count) AS study_count,
                        SUM(session_row.correct_count) AS correct_count,
                        MAX(session_row.submitted_at) AS activity_at
                    FROM kyyy_reading_session session_row
                    WHERE session_row.session_status = 'submitted'
                      AND session_row.answered_count > 0
                      AND session_row.user_id = #{userId}
                    <if test='startAt != null'>
                        AND session_row.submitted_at <![CDATA[>=]]> #{startAt}
                    </if>
                    <if test='endAt != null'>
                        AND session_row.submitted_at <![CDATA[<=]]> #{endAt}
                    </if>
                    GROUP BY session_row.user_id
                ) combined
                INNER JOIN app_user u ON u.id = combined.user_id AND u.status = 1
                GROUP BY combined.user_id, u.nickname, u.avatar_url
            ) agg
            LIMIT 1
            </script>
            """)
    KyyyLeaderboardAggregateRecord selectUserAggregateRecord(@Param("startAt") LocalDateTime startAt,
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
                        combined.user_id,
                        SUM(combined.study_count) AS study_count,
                        ROUND(SUM(combined.correct_count) * 100.0 / SUM(combined.study_count), 2) AS accuracy_rate,
                        MAX(combined.activity_at) AS last_practice_at
                    FROM (
                        SELECT
                            session_row.user_id,
                            SUM(session_row.completed_cards) AS study_count,
                            SUM(session_row.known_count) AS correct_count,
                            MAX(COALESCE(session_row.last_answered_at, session_row.finished_at)) AS activity_at
                        FROM kyyy_word_practice_session session_row
                        INNER JOIN app_user u ON u.id = session_row.user_id AND u.status = 1
                        WHERE session_row.status = 'completed'
                          AND session_row.completed_cards > 0
                        <if test='startAt != null'>
                            AND session_row.finished_at <![CDATA[>=]]> #{startAt}
                        </if>
                        <if test='endAt != null'>
                            AND session_row.finished_at <![CDATA[<=]]> #{endAt}
                        </if>
                        GROUP BY session_row.user_id
                        UNION ALL
                        SELECT
                            session_row.user_id,
                            SUM(session_row.answered_count) AS study_count,
                            SUM(session_row.correct_count) AS correct_count,
                            MAX(session_row.submitted_at) AS activity_at
                        FROM kyyy_reading_session session_row
                        INNER JOIN app_user u ON u.id = session_row.user_id AND u.status = 1
                        WHERE session_row.session_status = 'submitted'
                          AND session_row.answered_count > 0
                        <if test='startAt != null'>
                            AND session_row.submitted_at <![CDATA[>=]]> #{startAt}
                        </if>
                        <if test='endAt != null'>
                            AND session_row.submitted_at <![CDATA[<=]]> #{endAt}
                        </if>
                        GROUP BY session_row.user_id
                    ) combined
                    GROUP BY combined.user_id
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
            SELECT COUNT(DISTINCT participants.user_id)
            FROM (
                SELECT session_row.user_id
                FROM kyyy_word_practice_session session_row
                INNER JOIN app_user u ON u.id = session_row.user_id AND u.status = 1
                WHERE session_row.status = 'completed'
                  AND session_row.completed_cards > 0
                <if test='startAt != null'>
                    AND session_row.finished_at <![CDATA[>=]]> #{startAt}
                </if>
                <if test='endAt != null'>
                    AND session_row.finished_at <![CDATA[<=]]> #{endAt}
                </if>
                UNION
                SELECT session_row.user_id
                FROM kyyy_reading_session session_row
                INNER JOIN app_user u ON u.id = session_row.user_id AND u.status = 1
                WHERE session_row.session_status = 'submitted'
                  AND session_row.answered_count > 0
                <if test='startAt != null'>
                    AND session_row.submitted_at <![CDATA[>=]]> #{startAt}
                </if>
                <if test='endAt != null'>
                    AND session_row.submitted_at <![CDATA[<=]]> #{endAt}
                </if>
            ) participants
            </script>
            """)
    Long countParticipants(@Param("startAt") LocalDateTime startAt,
                           @Param("endAt") LocalDateTime endAt);
}
