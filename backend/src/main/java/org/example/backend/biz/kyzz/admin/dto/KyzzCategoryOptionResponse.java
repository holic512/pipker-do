package org.example.backend.biz.kyzz.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KyzzCategoryOptionResponse {

    private Long id;

    private String categoryCode;

    private String categoryName;

    private Integer categoryLevel;

    private Integer isEnabled;
}
