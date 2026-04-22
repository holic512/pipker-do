package org.example.backend.shared.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminUserCreateRequest {

    private String username;

    private String displayName;

    private String password;

    private String confirmPassword;

    private Integer status;

    private String defaultProjectCode;

    private List<Long> roleIds;

    private List<String> projectCodes;
}
