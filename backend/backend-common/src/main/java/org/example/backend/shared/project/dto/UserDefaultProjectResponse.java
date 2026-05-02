package org.example.backend.shared.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDefaultProjectResponse {

    private String projectCode;

    private String projectName;

    private String lastVisitAt;
}
