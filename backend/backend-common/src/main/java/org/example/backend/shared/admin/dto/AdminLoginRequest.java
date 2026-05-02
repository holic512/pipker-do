package org.example.backend.shared.admin.dto;

import lombok.Data;

@Data
public class AdminLoginRequest {

    private String username;

    private String password;
}
