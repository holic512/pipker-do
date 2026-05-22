/**
 * @file KyyyUserReadingWrongQuestion
 * @project pipker-do
 * @module 考研英语 / 阅读模块
 * @description 映射用户阅读错题聚合表，支撑错题本查询与掌握状态追踪。
 * @logic 1. 按用户和题目聚合错题；2. 统计首次、最近错题时间和累计次数；3. 记录掌握状态与掌握时间。
 * @dependencies Table: kyyy_user_reading_wrong_question
 * @index_tags 考研英语, 错题本, 阅读错题, 掌握状态
 * @author holic512
 */
package org.example.backend.biz.kyyy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("kyyy_user_reading_wrong_question")
public class KyyyUserReadingWrongQuestion implements Serializable {

    /** 用户阅读错题主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID。 */
    private Long userId;

    /** 阅读文章 ID。 */
    private Long passageId;

    /** 阅读题目 ID。 */
    private Long questionId;

    /** 首次错题时间。 */
    private LocalDateTime firstWrongAt;

    /** 最近错题时间。 */
    private LocalDateTime lastWrongAt;

    /** 累计错题次数。 */
    private Integer wrongCount;

    /** 是否掌握。 */
    private Integer isMastered;

    /** 掌握时间。 */
    private LocalDateTime masteredAt;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
