package org.example.backend.shared.project.support;

import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 索引: 全局项目编码目录。
 */
public final class ProjectCatalog {

    public static final String DEFAULT_PROJECT_CODE = "kyzz";

    private static final Map<String, ProjectDescriptor> PROJECTS = new LinkedHashMap<>();

    static {
        register("kyzz", "考研政治");
        register("kysx", "考研数学");
        register("kyyy", "考研英语");
    }

    private ProjectCatalog() {
    }

    public static boolean supports(String projectCode) {
        return StringUtils.hasText(projectCode) && PROJECTS.containsKey(projectCode.trim());
    }

    public static ProjectDescriptor getOrDefault(String projectCode) {
        if (supports(projectCode)) {
            return PROJECTS.get(projectCode.trim());
        }
        return PROJECTS.get(DEFAULT_PROJECT_CODE);
    }

    public static List<ProjectDescriptor> allProjects() {
        return List.copyOf(PROJECTS.values());
    }

    private static void register(String code, String name) {
        PROJECTS.put(code, new ProjectDescriptor(code, name));
    }

    public record ProjectDescriptor(String code, String name) {
    }
}
