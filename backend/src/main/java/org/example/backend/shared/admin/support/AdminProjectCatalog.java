package org.example.backend.shared.admin.support;

import org.example.backend.shared.admin.dto.AdminProjectResponse;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AdminProjectCatalog {

    private static final Map<String, String> PROJECT_NAMES = new LinkedHashMap<>();

    static {
        PROJECT_NAMES.put("kyzz", "考研政治");
        PROJECT_NAMES.put("kysx", "考研数学");
    }

    private AdminProjectCatalog() {
    }

    public static boolean supports(String projectCode) {
        return PROJECT_NAMES.containsKey(projectCode);
    }

    public static AdminProjectResponse toProject(String projectCode, boolean enabled) {
        return new AdminProjectResponse(projectCode, PROJECT_NAMES.getOrDefault(projectCode, projectCode), enabled);
    }

    public static List<AdminProjectResponse> allProjects() {
        return PROJECT_NAMES.keySet().stream()
                .map(projectCode -> toProject(projectCode, true))
                .toList();
    }
}
