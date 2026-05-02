package org.example.backend.shared.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminUserRoleAssignmentRequest {

    private List<Long> roleIds;
}
