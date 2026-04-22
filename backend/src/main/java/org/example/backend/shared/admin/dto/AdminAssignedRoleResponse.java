package org.example.backend.shared.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminAssignedRoleResponse {

    private Long id;

    private String roleCode;

    private String roleName;

    private Integer status;

    private Boolean protectedRole;
}
