package org.example.backend.shared.admin.support;

import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AdminRoleCatalog {

    private static final Map<String, RoleProfile> ROLE_PROFILES;

    static {
        Map<String, RoleProfile> profiles = new LinkedHashMap<>();
        profiles.put("SUPER_ADMIN", new RoleProfile(
                "负责后台全局治理，拥有管理员、角色、项目授权和全部业务数据的最高权限。",
                List.of("管理所有管理员账号", "管理角色定义与启停", "配置全部项目访问范围", "访问所有业务管理菜单"),
                1,
                true
        ));
        profiles.put("SYSTEM_ADMIN", new RoleProfile(
                "负责后台通用治理，侧重管理员、角色和项目权限的日常维护。",
                List.of("管理普通管理员账号", "维护角色状态与说明", "配置项目授权范围", "访问通用管理菜单"),
                2,
                false
        ));
        profiles.put("PROJECT_ADMIN", new RoleProfile(
                "负责单个或多个业务项目的后台运营与配置，不处理系统级账号治理。",
                List.of("访问被授权项目菜单", "维护项目业务内容", "管理项目内运营配置", "查看项目数据概览"),
                3,
                false
        ));
        profiles.put("CONTENT_OPERATOR", new RoleProfile(
                "负责题库、分类、标签等内容维护工作，强调内容编辑而非系统治理。",
                List.of("维护题库与标签", "处理内容上下架", "查看内容运营数据", "限制在授权项目内操作"),
                4,
                false
        ));
        profiles.put("DATA_VIEWER", new RoleProfile(
                "负责查看后台数据和报表，不参与新增、修改、删除等写操作。",
                List.of("查看工作台与报表", "查看列表与详情", "不能执行写操作", "限制在授权项目内访问"),
                5,
                false
        ));
        ROLE_PROFILES = Collections.unmodifiableMap(profiles);
    }

    private AdminRoleCatalog() {
    }

    public static String normalizeRoleCode(String roleCode) {
        if (!StringUtils.hasText(roleCode)) {
            return "";
        }
        return roleCode.trim().toUpperCase(Locale.ROOT);
    }

    public static boolean isProtectedRole(String roleCode) {
        return getProfile(roleCode).protectedRole();
    }

    public static boolean isBuiltInRole(String roleCode) {
        return ROLE_PROFILES.containsKey(normalizeRoleCode(roleCode));
    }

    public static String descriptionOf(String roleCode) {
        RoleProfile profile = getProfile(roleCode);
        return profile.description();
    }

    public static List<String> capabilitiesOf(String roleCode) {
        RoleProfile profile = getProfile(roleCode);
        return profile.capabilities();
    }

    public static int sortOrderOf(String roleCode) {
        return getProfile(roleCode).sortOrder();
    }

    private static RoleProfile getProfile(String roleCode) {
        return ROLE_PROFILES.getOrDefault(normalizeRoleCode(roleCode), new RoleProfile(
                "自定义角色，当前系统按角色编码进行能力约定，后续可继续扩展更细粒度权限。",
                List.of("可参与管理员角色分配", "可结合项目授权限制范围", "当前不区分按钮级权限"),
                999,
                false
        ));
    }

    private record RoleProfile(String description, List<String> capabilities, int sortOrder, boolean protectedRole) {
    }
}
