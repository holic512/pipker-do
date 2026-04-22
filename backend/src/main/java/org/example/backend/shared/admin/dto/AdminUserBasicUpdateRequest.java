package org.example.backend.shared.admin.dto;

import lombok.Data;

@Data
public class AdminUserBasicUpdateRequest {

    private String displayName;

    private String defaultProjectCode;
}
