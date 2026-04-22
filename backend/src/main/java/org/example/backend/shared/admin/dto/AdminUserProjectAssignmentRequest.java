package org.example.backend.shared.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminUserProjectAssignmentRequest {

    private List<String> projectCodes;

    private String defaultProjectCode;
}
