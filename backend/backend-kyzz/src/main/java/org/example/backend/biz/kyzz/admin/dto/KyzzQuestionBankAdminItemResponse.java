package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class KyzzQuestionBankAdminItemResponse {

    private Long id;

    private String bankCode;

    private String bankName;

    private String subtitle;

    private String coverUrl;

    private String coverStorageKey;

    private String description;

    private Long categoryId;

    private String categoryName;

    private Integer categoryLevel;

    private Integer difficultyLevel;

    private Integer questionCount;

    private Integer actualQuestionCount;

    private BigDecimal totalScore;

    private Integer ratingCount;

    private Integer collectCount;

    private Integer studyUserCount;

    private Integer status;

    private Integer sortNo;

    private Long createdBy;

    private String createdByDisplayName;

    private Boolean canDelete;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
