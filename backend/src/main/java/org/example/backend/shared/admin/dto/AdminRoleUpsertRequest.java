package org.example.backend.shared.admin.dto;

import lombok.Data;

@Data
public class AdminRoleUpsertRequest {

    private String roleCode;

    private String roleName;

    private Integer status;
}
