package org.example.backend.shared.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.admin.dto.AdminAssignedRoleResponse;
import org.example.backend.shared.admin.dto.AdminProjectResponse;
import org.example.backend.shared.admin.dto.AdminUserBasicUpdateRequest;
import org.example.backend.shared.admin.dto.AdminUserCreateRequest;
import org.example.backend.shared.admin.dto.AdminUserPasswordResetRequest;
import org.example.backend.shared.admin.dto.AdminUserProjectAssignmentRequest;
import org.example.backend.shared.admin.dto.AdminUserRoleAssignmentRequest;
import org.example.backend.shared.admin.dto.AdminUserStatusUpdateRequest;
import org.example.backend.shared.admin.dto.AdminUserSummaryResponse;
import org.example.backend.shared.admin.entity.AdminProjectAccess;
import org.example.backend.shared.admin.entity.AdminRole;
import org.example.backend.shared.admin.entity.AdminUser;
import org.example.backend.shared.admin.entity.AdminUserRole;
import org.example.backend.shared.admin.mapper.AdminProjectAccessMapper;
import org.example.backend.shared.admin.mapper.AdminRoleMapper;
import org.example.backend.shared.admin.mapper.AdminUserMapper;
import org.example.backend.shared.admin.mapper.AdminUserRoleMapper;
import org.example.backend.shared.admin.support.AdminProjectCatalog;
import org.example.backend.shared.admin.support.AdminRoleCatalog;
import org.example.backend.util.password.SCryptUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AdminUserManagementService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{3,49}$");

    private final AdminUserMapper adminUserMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final AdminProjectAccessMapper adminProjectAccessMapper;

    public AdminUserManagementService(AdminUserMapper adminUserMapper,
                                      AdminRoleMapper adminRoleMapper,
                                      AdminUserRoleMapper adminUserRoleMapper,
                                      AdminProjectAccessMapper adminProjectAccessMapper) {
        this.adminUserMapper = adminUserMapper;
        this.adminRoleMapper = adminRoleMapper;
        this.adminUserRoleMapper = adminUserRoleMapper;
        this.adminProjectAccessMapper = adminProjectAccessMapper;
    }

    public List<AdminUserSummaryResponse> listAdmins(Long operatorId,
                                                     String keyword,
                                                     Integer status,
                                                     String roleCode,
                                                     String projectCode) {
        requireAdminManager(operatorId);

        LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<AdminUser>()
                .orderByDesc(AdminUser::getId);
        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();
            queryWrapper.and(wrapper -> wrapper.like(AdminUser::getUsername, trimmedKeyword)
                    .or()
                    .like(AdminUser::getDisplayName, trimmedKeyword));
        }
        if (status != null) {
            validateStatus(status);
            queryWrapper.eq(AdminUser::getStatus, status);
        }

        List<AdminUser> admins = adminUserMapper.selectList(queryWrapper);
        if (admins.isEmpty()) {
            return List.of();
        }

        AdminUserAggregate aggregate = loadAggregate(admins.stream().map(AdminUser::getId).toList());
        String normalizedRoleCode = AdminRoleCatalog.normalizeRoleCode(roleCode);
        String normalizedProjectCode = normalizeProjectCode(projectCode);

        return admins.stream()
                .map(admin -> toSummary(admin, aggregate))
                .filter(item -> !StringUtils.hasText(normalizedRoleCode)
                        || item.getRoles().stream().anyMatch(role -> normalizedRoleCode.equals(AdminRoleCatalog.normalizeRoleCode(role.getRoleCode()))))
                .filter(item -> !StringUtils.hasText(normalizedProjectCode)
                        || item.getProjects().stream().anyMatch(project -> normalizedProjectCode.equals(project.getCode())))
                .toList();
    }

    public AdminUserSummaryResponse getAdminDetail(Long operatorId, Long userId) {
        requireAdminManager(operatorId);
        AdminUser adminUser = requireAdminUser(userId);
        return toSummary(adminUser, loadAggregate(List.of(userId)));
    }

    public List<AdminProjectResponse> listProjectCatalog(Long operatorId) {
        requireAdminManager(operatorId);
        return AdminProjectCatalog.allProjects();
    }

    @Transactional
    public AdminUserSummaryResponse createAdmin(Long operatorId, AdminUserCreateRequest request) {
        OperatorPermission permission = requireAdminManager(operatorId);
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "管理员参数不能为空");
        }

        String username = normalizeUsername(request.getUsername());
        String displayName = normalizeDisplayName(request.getDisplayName());
        Integer status = request.getStatus() == null ? 1 : request.getStatus();
        String password = request.getPassword() == null ? "" : request.getPassword();
        String confirmPassword = request.getConfirmPassword() == null ? "" : request.getConfirmPassword();

        validateUsername(username);
        validateDisplayName(displayName);
        validateStatus(status);
        validatePasswordPair(password, confirmPassword, username);

        boolean exists = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, username)) > 0;
        if (exists) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "登录账号已存在");
        }

        List<AdminRole> assignedRoles = resolveAssignedRoles(permission, request.getRoleIds());
        List<String> projectCodes = resolveProjectCodes(request.getProjectCodes());
        String defaultProjectCode = resolveDefaultProjectCode(request.getDefaultProjectCode(), projectCodes);

        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(username);
        adminUser.setDisplayName(displayName);
        adminUser.setPasswordHash(SCryptUtil.hashPassword(password));
        adminUser.setStatus(status);
        adminUser.setDefaultProjectCode(defaultProjectCode);
        adminUserMapper.insert(adminUser);

        replaceUserRoles(adminUser.getId(), assignedRoles.stream().map(AdminRole::getId).toList());
        replaceUserProjects(adminUser.getId(), projectCodes);
        return getAdminDetail(operatorId, adminUser.getId());
    }

    @Transactional
    public AdminUserSummaryResponse updateBasicInfo(Long operatorId, Long userId, AdminUserBasicUpdateRequest request) {
        OperatorPermission permission = requireAdminManager(operatorId);
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "管理员资料不能为空");
        }

        AdminUser adminUser = requireAdminUser(userId);
        Set<String> currentRoleCodes = getAssignedRoleCodes(userId);
        ensureProtectedAccountEditable(permission, currentRoleCodes);

        String displayName = normalizeDisplayName(request.getDisplayName());
        validateDisplayName(displayName);

        List<String> currentProjectCodes = getEnabledProjectCodes(userId);
        if (currentProjectCodes.isEmpty()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前管理员还没有配置项目权限，请先完成项目授权");
        }
        String defaultProjectCode = resolveDefaultProjectCode(request.getDefaultProjectCode(), currentProjectCodes);

        adminUserMapper.update(null, new LambdaUpdateWrapper<AdminUser>()
                .eq(AdminUser::getId, userId)
                .set(AdminUser::getDisplayName, displayName)
                .set(AdminUser::getDefaultProjectCode, defaultProjectCode));
        return getAdminDetail(operatorId, adminUser.getId());
    }

    @Transactional
    public AdminUserSummaryResponse updateStatus(Long operatorId, Long userId, AdminUserStatusUpdateRequest request) {
        OperatorPermission permission = requireAdminManager(operatorId);
        if (request == null || request.getStatus() == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "管理员状态不能为空");
        }

        AdminUser adminUser = requireAdminUser(userId);
        validateStatus(request.getStatus());
        Set<String> currentRoleCodes = getAssignedRoleCodes(userId);
        ensureProtectedAccountEditable(permission, currentRoleCodes);

        if (userId.equals(operatorId) && request.getStatus() == 0) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "不能禁用当前登录中的管理员账号");
        }
        if (request.getStatus() == 1) {
            ensureUserReadyForEnable(userId);
        }
        if (adminUser.getStatus() != null && adminUser.getStatus() == 1
                && request.getStatus() == 0
                && currentRoleCodes.contains("SUPER_ADMIN")
                && adminUserRoleMapper.countActiveUsersByRoleCode("SUPER_ADMIN") <= 1) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "至少需要保留一个处于启用状态的超级管理员");
        }

        adminUserMapper.update(null, new LambdaUpdateWrapper<AdminUser>()
                .eq(AdminUser::getId, userId)
                .set(AdminUser::getStatus, request.getStatus()));
        return getAdminDetail(operatorId, userId);
    }

    @Transactional
    public AdminUserSummaryResponse updateRoles(Long operatorId, Long userId, AdminUserRoleAssignmentRequest request) {
        OperatorPermission permission = requireAdminManager(operatorId);
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "角色分配参数不能为空");
        }

        AdminUser adminUser = requireAdminUser(userId);
        Set<String> currentRoleCodes = getAssignedRoleCodes(userId);
        ensureProtectedAccountEditable(permission, currentRoleCodes);

        List<AdminRole> assignedRoles = resolveAssignedRoles(permission, request.getRoleIds());
        Set<String> nextRoleCodes = assignedRoles.stream()
                .map(AdminRole::getRoleCode)
                .map(AdminRoleCatalog::normalizeRoleCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (userId.equals(operatorId) && !containsManagementRole(nextRoleCodes)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "不能移除当前登录管理员唯一的后台管理角色");
        }
        if (adminUser.getStatus() != null && adminUser.getStatus() == 1
                && currentRoleCodes.contains("SUPER_ADMIN")
                && !nextRoleCodes.contains("SUPER_ADMIN")
                && adminUserRoleMapper.countActiveUsersByRoleCode("SUPER_ADMIN") <= 1) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "至少需要保留一个处于启用状态的超级管理员");
        }

        replaceUserRoles(userId, assignedRoles.stream().map(AdminRole::getId).toList());
        return getAdminDetail(operatorId, userId);
    }

    @Transactional
    public AdminUserSummaryResponse updateProjects(Long operatorId, Long userId, AdminUserProjectAssignmentRequest request) {
        OperatorPermission permission = requireAdminManager(operatorId);
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "项目授权参数不能为空");
        }

        requireAdminUser(userId);
        Set<String> currentRoleCodes = getAssignedRoleCodes(userId);
        ensureProtectedAccountEditable(permission, currentRoleCodes);

        List<String> projectCodes = resolveProjectCodes(request.getProjectCodes());
        String defaultProjectCode = resolveDefaultProjectCode(request.getDefaultProjectCode(), projectCodes);

        replaceUserProjects(userId, projectCodes);
        adminUserMapper.update(null, new LambdaUpdateWrapper<AdminUser>()
                .eq(AdminUser::getId, userId)
                .set(AdminUser::getDefaultProjectCode, defaultProjectCode));
        return getAdminDetail(operatorId, userId);
    }

    @Transactional
    public void resetPassword(Long operatorId, Long userId, AdminUserPasswordResetRequest request) {
        OperatorPermission permission = requireAdminManager(operatorId);
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "重置密码参数不能为空");
        }

        AdminUser adminUser = requireAdminUser(userId);
        Set<String> currentRoleCodes = getAssignedRoleCodes(userId);
        ensureProtectedAccountEditable(permission, currentRoleCodes);

        String password = request.getPassword() == null ? "" : request.getPassword();
        String confirmPassword = request.getConfirmPassword() == null ? "" : request.getConfirmPassword();
        validatePasswordPair(password, confirmPassword, adminUser.getUsername());

        adminUserMapper.update(null, new LambdaUpdateWrapper<AdminUser>()
                .eq(AdminUser::getId, userId)
                .set(AdminUser::getPasswordHash, SCryptUtil.hashPassword(password)));
    }

    private AdminUserAggregate loadAggregate(Collection<Long> userIds) {
        if (userIds.isEmpty()) {
            return new AdminUserAggregate(Collections.emptyMap(), Collections.emptyMap());
        }

        List<AdminRole> allRoles = adminRoleMapper.selectList(new LambdaQueryWrapper<AdminRole>().orderByAsc(AdminRole::getId));
        Map<Long, AdminRole> roleMap = allRoles.stream()
                .collect(Collectors.toMap(AdminRole::getId, Function.identity()));

        Map<Long, List<AdminAssignedRoleResponse>> rolesByUserId = adminUserRoleMapper.selectList(new LambdaQueryWrapper<AdminUserRole>()
                        .in(AdminUserRole::getUserId, userIds)
                        .orderByAsc(AdminUserRole::getId))
                .stream()
                .map(relation -> {
                    AdminRole role = roleMap.get(relation.getRoleId());
                    if (role == null) {
                        return null;
                    }
                    return Map.entry(relation.getUserId(), new AdminAssignedRoleResponse(
                            role.getId(),
                            role.getRoleCode(),
                            role.getRoleName(),
                            role.getStatus(),
                            AdminRoleCatalog.isProtectedRole(role.getRoleCode())
                    ));
                })
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toCollection(ArrayList::new))));

        Map<Long, List<AdminProjectResponse>> projectsByUserId = adminProjectAccessMapper.selectList(new LambdaQueryWrapper<AdminProjectAccess>()
                        .in(AdminProjectAccess::getUserId, userIds)
                        .eq(AdminProjectAccess::getEnabled, 1)
                        .orderByAsc(AdminProjectAccess::getId))
                .stream()
                .filter(access -> AdminProjectCatalog.supports(access.getProjectCode()))
                .collect(Collectors.groupingBy(AdminProjectAccess::getUserId,
                        Collectors.mapping(access -> AdminProjectCatalog.toProject(access.getProjectCode(), true),
                                Collectors.toCollection(ArrayList::new))));

        rolesByUserId.replaceAll((userId, values) -> values.stream()
                .sorted(Comparator
                        .comparing((AdminAssignedRoleResponse item) -> AdminRoleCatalog.sortOrderOf(item.getRoleCode()))
                        .thenComparing(AdminAssignedRoleResponse::getId))
                .toList());

        return new AdminUserAggregate(rolesByUserId, projectsByUserId);
    }

    private AdminUserSummaryResponse toSummary(AdminUser adminUser, AdminUserAggregate aggregate) {
        List<AdminAssignedRoleResponse> roles = aggregate.rolesByUserId().getOrDefault(adminUser.getId(), List.of());
        List<AdminProjectResponse> projects = aggregate.projectsByUserId().getOrDefault(adminUser.getId(), List.of());
        boolean protectedAccount = roles.stream().anyMatch(role -> Boolean.TRUE.equals(role.getProtectedRole()));
        String defaultProjectCode = adminUser.getDefaultProjectCode();
        String defaultProjectName = StringUtils.hasText(defaultProjectCode) && AdminProjectCatalog.supports(defaultProjectCode)
                ? AdminProjectCatalog.toProject(defaultProjectCode, true).getName()
                : null;

        return new AdminUserSummaryResponse(
                adminUser.getId(),
                adminUser.getUsername(),
                adminUser.getDisplayName(),
                adminUser.getStatus(),
                defaultProjectCode,
                defaultProjectName,
                roles,
                projects,
                protectedAccount,
                adminUser.getLastLoginAt(),
                adminUser.getCreatedAt(),
                adminUser.getUpdatedAt()
        );
    }

    private AdminUser requireAdminUser(Long userId) {
        AdminUser adminUser = adminUserMapper.selectById(userId);
        if (adminUser == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "管理员不存在");
        }
        return adminUser;
    }

    private OperatorPermission requireAdminManager(Long operatorId) {
        Set<String> roleCodes = adminRoleMapper.selectActiveRolesByUserId(operatorId).stream()
                .map(AdminRole::getRoleCode)
                .map(AdminRoleCatalog::normalizeRoleCode)
                .collect(Collectors.toSet());

        boolean isSuperAdmin = roleCodes.contains("SUPER_ADMIN");
        boolean isSystemAdmin = roleCodes.contains("SYSTEM_ADMIN");
        if (!isSuperAdmin && !isSystemAdmin) {
            throw new BusinessException(ApiResponseCode.FORBIDDEN, "当前管理员没有管理员管理权限");
        }
        return new OperatorPermission(isSuperAdmin, roleCodes);
    }

    private void ensureProtectedAccountEditable(OperatorPermission permission, Set<String> targetRoleCodes) {
        boolean protectedAccount = targetRoleCodes.stream().anyMatch(AdminRoleCatalog::isProtectedRole);
        if (protectedAccount && !permission.isSuperAdmin()) {
            throw new BusinessException(ApiResponseCode.FORBIDDEN, "只有超级管理员可以修改受保护管理员账号");
        }
    }

    private List<AdminRole> resolveAssignedRoles(OperatorPermission permission, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "请至少为管理员分配一个角色");
        }

        List<Long> distinctIds = roleIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (distinctIds.isEmpty()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "请至少为管理员分配一个角色");
        }

        List<AdminRole> roles = adminRoleMapper.selectList(new LambdaQueryWrapper<AdminRole>()
                .in(AdminRole::getId, distinctIds));
        if (roles.size() != distinctIds.size()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "所选角色不存在或已失效");
        }
        if (roles.stream().anyMatch(role -> role.getStatus() == null || role.getStatus() != 1)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "不能为管理员分配已停用角色");
        }
        if (!permission.isSuperAdmin() && roles.stream().anyMatch(role -> AdminRoleCatalog.isProtectedRole(role.getRoleCode()))) {
            throw new BusinessException(ApiResponseCode.FORBIDDEN, "当前管理员不能分配受保护角色");
        }
        return roles.stream()
                .sorted(Comparator
                        .comparing((AdminRole role) -> AdminRoleCatalog.sortOrderOf(role.getRoleCode()))
                        .thenComparing(AdminRole::getId))
                .toList();
    }

    private List<String> resolveProjectCodes(List<String> projectCodes) {
        if (projectCodes == null || projectCodes.isEmpty()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "请至少为管理员分配一个项目");
        }
        List<String> normalizedCodes = projectCodes.stream()
                .filter(StringUtils::hasText)
                .map(this::normalizeProjectCode)
                .distinct()
                .toList();
        if (normalizedCodes.isEmpty()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "请至少为管理员分配一个项目");
        }
        if (normalizedCodes.stream().anyMatch(code -> !AdminProjectCatalog.supports(code))) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "存在不支持的项目编码");
        }
        return normalizedCodes;
    }

    private String resolveDefaultProjectCode(String defaultProjectCode, List<String> projectCodes) {
        String normalizedDefault = normalizeProjectCode(defaultProjectCode);
        if (!StringUtils.hasText(normalizedDefault)) {
            return projectCodes.get(0);
        }
        if (!projectCodes.contains(normalizedDefault)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "默认项目必须来自当前授权项目列表");
        }
        return normalizedDefault;
    }

    private void ensureUserReadyForEnable(Long userId) {
        if (getAssignedRoleCodes(userId).isEmpty()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前管理员还没有分配角色，不能直接启用");
        }
        if (getEnabledProjectCodes(userId).isEmpty()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前管理员还没有配置项目权限，不能直接启用");
        }
    }

    private Set<String> getAssignedRoleCodes(Long userId) {
        List<Long> roleIds = adminUserRoleMapper.selectList(new LambdaQueryWrapper<AdminUserRole>()
                        .eq(AdminUserRole::getUserId, userId))
                .stream()
                .map(AdminUserRole::getRoleId)
                .distinct()
                .toList();
        if (roleIds.isEmpty()) {
            return Set.of();
        }
        return adminRoleMapper.selectList(new LambdaQueryWrapper<AdminRole>()
                        .in(AdminRole::getId, roleIds))
                .stream()
                .map(AdminRole::getRoleCode)
                .map(AdminRoleCatalog::normalizeRoleCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<String> getEnabledProjectCodes(Long userId) {
        return adminProjectAccessMapper.selectList(new LambdaQueryWrapper<AdminProjectAccess>()
                        .eq(AdminProjectAccess::getUserId, userId)
                        .eq(AdminProjectAccess::getEnabled, 1)
                        .orderByAsc(AdminProjectAccess::getId))
                .stream()
                .map(AdminProjectAccess::getProjectCode)
                .filter(AdminProjectCatalog::supports)
                .distinct()
                .toList();
    }

    private void replaceUserRoles(Long userId, List<Long> roleIds) {
        adminUserRoleMapper.delete(new LambdaQueryWrapper<AdminUserRole>()
                .eq(AdminUserRole::getUserId, userId));

        roleIds.forEach(roleId -> {
            AdminUserRole relation = new AdminUserRole();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            adminUserRoleMapper.insert(relation);
        });
    }

    private void replaceUserProjects(Long userId, List<String> projectCodes) {
        adminProjectAccessMapper.delete(new LambdaQueryWrapper<AdminProjectAccess>()
                .eq(AdminProjectAccess::getUserId, userId));

        projectCodes.forEach(projectCode -> {
            AdminProjectAccess access = new AdminProjectAccess();
            access.setUserId(userId);
            access.setProjectCode(projectCode);
            access.setEnabled(1);
            adminProjectAccessMapper.insert(access);
        });
    }

    private boolean containsManagementRole(Set<String> roleCodes) {
        return roleCodes.contains("SUPER_ADMIN") || roleCodes.contains("SYSTEM_ADMIN");
    }

    private void validatePasswordPair(String password, String confirmPassword, String username) {
        if (!StringUtils.hasText(password) || !StringUtils.hasText(confirmPassword)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "密码和确认密码不能为空");
        }
        if (!Objects.equals(password, confirmPassword)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "两次输入的密码不一致");
        }
        if (StringUtils.hasText(username) && password.toLowerCase(Locale.ROOT).contains(username.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "密码不能包含账号名");
        }
        if (password.length() < 8 || password.length() > 32) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "密码长度需保持在 8 到 32 位之间");
        }

        int strength = 0;
        if (password.chars().anyMatch(Character::isLowerCase)) {
            strength++;
        }
        if (password.chars().anyMatch(Character::isUpperCase)) {
            strength++;
        }
        if (password.chars().anyMatch(Character::isDigit)) {
            strength++;
        }
        if (password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch))) {
            strength++;
        }
        if (strength < 3) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "密码需至少包含大写字母、小写字母、数字、符号中的 3 类");
        }
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim();
    }

    private void validateUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "登录账号不能为空");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "登录账号需为 4 到 50 位字母、数字或下划线，且必须以字母开头");
        }
    }

    private String normalizeDisplayName(String displayName) {
        return displayName == null ? "" : displayName.trim();
    }

    private void validateDisplayName(String displayName) {
        if (!StringUtils.hasText(displayName)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "显示名称不能为空");
        }
        if (displayName.length() < 2 || displayName.length() > 50) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "显示名称长度需保持在 2 到 50 个字符之间");
        }
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "管理员状态仅支持 0 或 1");
        }
    }

    private String normalizeProjectCode(String projectCode) {
        return projectCode == null ? "" : projectCode.trim().toLowerCase(Locale.ROOT);
    }

    private record OperatorPermission(boolean isSuperAdmin, Set<String> roleCodes) {
    }

    private record AdminUserAggregate(Map<Long, List<AdminAssignedRoleResponse>> rolesByUserId,
                                      Map<Long, List<AdminProjectResponse>> projectsByUserId) {
    }
}
