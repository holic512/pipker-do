package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 索引: KYYY 用户侧词库卡片。
 */
@Data
@AllArgsConstructor
public class KyyyWordBankRecordResponse implements Serializable {

    private Long id;

    private String bankCode;

    private String bankName;

    private String subtitle;

    private String description;

    private Integer wordCount;

    private Integer studyUserCount;

    private Integer sortNo;

    private String joinSource;

    private LocalDateTime joinedAt;

    private LocalDateTime lastPracticeAt;

    private Boolean selected;
}
