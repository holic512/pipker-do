package org.example.backend.shared.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class AdminRoleSummaryResponse {

    private Long id;

    private String roleCode;

    private String roleName;

    private Integer status;

    private Integer adminCount;

    private String description;

    private List<String> capabilities;

    private Boolean protectedRole;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
