package org.example.backend.shared.admin.dto;

import lombok.Data;

@Data
public class AdminProfileUpdateRequest {

    private String displayName;

    private String defaultProjectCode;
}
