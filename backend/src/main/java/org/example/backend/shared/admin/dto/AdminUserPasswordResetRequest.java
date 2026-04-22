package org.example.backend.shared.admin.dto;

import lombok.Data;

@Data
public class AdminUserPasswordResetRequest {

    private String password;

    private String confirmPassword;
}
