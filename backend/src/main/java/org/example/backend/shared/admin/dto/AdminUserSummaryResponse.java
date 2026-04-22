package org.example.backend.shared.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class AdminUserSummaryResponse {

    private Long id;

    private String username;

    private String displayName;

    private Integer status;

    private String defaultProjectCode;

    private String defaultProjectName;

    private List<AdminAssignedRoleResponse> roles;

    private List<AdminProjectResponse> projects;

    private Boolean protectedAccount;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
