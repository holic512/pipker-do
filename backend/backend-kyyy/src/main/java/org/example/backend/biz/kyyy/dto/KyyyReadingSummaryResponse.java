/**
 * @file KyyyReadingSummaryResponse
 * @project pipker-do
 * @module 考研英语 / 阅读做题
 * @description 返回阅读交卷后的结果汇总。
 * @logic 1. 输出答对与答错数量；2. 输出正确率；3. 输出交卷时间。
 * @dependencies DTO: KyyyReadingSessionResponse
 * @index_tags 考研英语, 阅读结果, 交卷汇总, 正确率
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class KyyyReadingSummaryResponse implements Serializable {

    private Integer correctCount;

    private Integer wrongCount;

    private BigDecimal accuracyRate;

    private LocalDateTime submittedAt;
}
