/**
 * @file KyyyReadingPassage
 * @project pipker-do
 * @module 考研英语 / 阅读模块
 * @description 映射阅读文章主表，承载英一英二篇章正文、来源信息与统计字段。
 * @logic 1. 归档文章全文与来源；2. 绑定考试方向与卷内篇次；3. 维护题目数和总分汇总。
 * @dependencies Table: kyyy_reading_passage
 * @index_tags 考研英语, 阅读文章, 英一英二, 篇章题
 * @author holic512
 */
package org.example.backend.biz.kyyy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("kyyy_reading_passage")
public class KyyyReadingPassage implements Serializable {

    /** 阅读文章主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 阅读文章编码。 */
    private String passageCode;

    /** 考试方向。 */
    private String examDirection;

    /** 来源年份。 */
    private Integer sourceYear;

    /** 来源名称。 */
    private String sourceName;

    /** 卷内篇次。 */
    private Integer passageNo;

    /** 文章标题。 */
    private String title;

    /** 阅读文章全文。 */
    private String passageText;

    /** 题目数。 */
    private Integer questionCount;

    /** 总分。 */
    private BigDecimal totalScore;

    /** 状态。 */
    private Integer status;

    /** 排序值。 */
    private Integer sortNo;

    /** 创建后台管理员 ID。 */
    private Long createdBy;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
