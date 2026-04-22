package org.example.backend.shared.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AdminCurrentUserResponse {

    private Long id;

    private String username;

    private String displayName;

    private List<String> roles;

    private List<AdminProjectResponse> projects;

    private String defaultProjectCode;
}
