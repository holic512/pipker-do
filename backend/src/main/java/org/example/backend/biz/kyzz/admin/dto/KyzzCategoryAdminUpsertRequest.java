package org.example.backend.biz.kyzz.admin.dto;

import lombok.Data;

@Data
public class KyzzCategoryAdminUpsertRequest {

    private String categoryCode;

    private String categoryName;

    private Integer categoryLevel;

    private Integer sortNo;

    private Integer isEnabled;
}
