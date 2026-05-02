package org.example.backend.shared.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminLoginResponse {

    private String token;

    private AdminCurrentUserResponse admin;
}
