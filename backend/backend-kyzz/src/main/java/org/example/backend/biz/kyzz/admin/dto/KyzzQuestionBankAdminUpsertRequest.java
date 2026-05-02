package org.example.backend.biz.kyzz.admin.dto;

import lombok.Data;

@Data
public class KyzzQuestionBankAdminUpsertRequest {

    private String bankCode;

    private String bankName;

    private String subtitle;

    private String description;

    private Long categoryId;

    private Integer difficultyLevel;

    private Integer sortNo;

    private Integer status;
}
