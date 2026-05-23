/**
 * @file KyyyWrongQuestionRecordResponse
 * @project pipker-do
 * @module 考研英语 / 阅读错题本
 * @description 返回单条阅读错题记录及文章来源信息，供小程序错题本列表展示。
 * @logic 1. 输出题目与篇章主键；2. 输出题干与文章来源元数据；3. 输出错题次数和掌握状态。
 * @dependencies API: /api/kyyy/wrong-questions
 * @index_tags 考研英语, 阅读错题本, 错题记录, 文章来源
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class KyyyWrongQuestionRecordResponse implements Serializable {

    private Long questionId;

    private Long passageId;

    private Integer sourceYear;

    private String sourceName;

    private Integer passageNo;

    private String examDirection;

    private String examDirectionLabel;

    private String stem;

    private Integer wrongCount;

    private LocalDateTime lastWrongAt;

    private Boolean isMastered;

    private LocalDateTime masteredAt;
}
