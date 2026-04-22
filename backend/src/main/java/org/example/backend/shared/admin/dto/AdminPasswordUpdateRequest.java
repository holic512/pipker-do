package org.example.backend.shared.admin.dto;

import lombok.Data;

@Data
public class AdminPasswordUpdateRequest {

    private String currentPassword;

    private String newPassword;

    private String confirmPassword;
}
