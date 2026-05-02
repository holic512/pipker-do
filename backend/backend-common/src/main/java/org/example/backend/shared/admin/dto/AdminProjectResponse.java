package org.example.backend.shared.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminProjectResponse {

    private String code;

    private String name;

    private boolean enabled;
}
