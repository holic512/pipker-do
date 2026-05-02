package org.example.backend.shared.admin.support;

import org.example.backend.shared.admin.dto.AdminProjectResponse;
import org.example.backend.shared.project.support.ProjectCatalog;

import java.util.List;

public final class AdminProjectCatalog {

    private AdminProjectCatalog() {
    }

    public static boolean supports(String projectCode) {
        return ProjectCatalog.supports(projectCode);
    }

    public static AdminProjectResponse toProject(String projectCode, boolean enabled) {
        if (!ProjectCatalog.supports(projectCode)) {
            return new AdminProjectResponse(projectCode, projectCode, enabled);
        }
        ProjectCatalog.ProjectDescriptor project = ProjectCatalog.getOrDefault(projectCode);
        return new AdminProjectResponse(project.code(), project.name(), enabled);
    }

    public static List<AdminProjectResponse> allProjects() {
        return ProjectCatalog.allProjects().stream()
                .map(project -> new AdminProjectResponse(project.code(), project.name(), true))
                .toList();
    }
}
