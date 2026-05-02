package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class KyzzCategoryAdminItemResponse {

    private Long id;

    private String categoryCode;

    private String categoryName;

    private Integer categoryLevel;

    private Integer sortNo;

    private Integer isEnabled;

    private Integer questionBankCount;

    private Integer questionCount;

    private Boolean canDelete;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
