package org.example.backend.shared.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.admin.dto.AdminRoleStatusUpdateRequest;
import org.example.backend.shared.admin.dto.AdminRoleSummaryResponse;
import org.example.backend.shared.admin.dto.AdminRoleUpsertRequest;
import org.example.backend.shared.admin.entity.AdminRole;
import org.example.backend.shared.admin.entity.AdminUserRole;
import org.example.backend.shared.admin.mapper.AdminRoleMapper;
import org.example.backend.shared.admin.mapper.AdminUserRoleMapper;
import org.example.backend.shared.admin.support.AdminRoleCatalog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AdminRoleManagementService {

    private static final Pattern ROLE_CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]{1,49}$");

    private final AdminRoleMapper adminRoleMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;

    public AdminRoleManagementService(AdminRoleMapper adminRoleMapper, AdminUserRoleMapper adminUserRoleMapper) {
        this.adminRoleMapper = adminRoleMapper;
        this.adminUserRoleMapper = adminUserRoleMapper;
    }

    public List<AdminRoleSummaryResponse> listRoles(Long operatorId, String keyword, Integer status) {
        requireRoleManager(operatorId);

        LambdaQueryWrapper<AdminRole> queryWrapper = new LambdaQueryWrapper<AdminRole>()
                .orderByAsc(AdminRole::getId);
        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();
            queryWrapper.and(wrapper -> wrapper.like(AdminRole::getRoleCode, trimmedKeyword)
                    .or()
                    .like(AdminRole::getRoleName, trimmedKeyword));
        }
        if (status != null) {
            validateStatus(status);
            queryWrapper.eq(AdminRole::getStatus, status);
        }

        return adminRoleMapper.selectList(queryWrapper).stream()
                .map(this::toSummary)
                .sorted(Comparator
                        .comparing((AdminRoleSummaryResponse item) -> AdminRoleCatalog.sortOrderOf(item.getRoleCode()))
                        .thenComparing(AdminRoleSummaryResponse::getId))
                .toList();
    }

    @Transactional
    public AdminRoleSummaryResponse createRole(Long operatorId, AdminRoleUpsertRequest request) {
        requireRoleManager(operatorId);
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "角色参数不能为空");
        }

        String roleCode = AdminRoleCatalog.normalizeRoleCode(request.getRoleCode());
        String roleName = request.getRoleName() == null ? "" : request.getRoleName().trim();
        Integer status = request.getStatus() == null ? 1 : request.getStatus();

        validateRoleCode(roleCode);
        validateRoleName(roleName);
        validateStatus(status);

        boolean exists = adminRoleMapper.selectCount(new LambdaQueryWrapper<AdminRole>()
                .eq(AdminRole::getRoleCode, roleCode)) > 0;
        if (exists) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "角色编码已存在");
        }

        AdminRole role = new AdminRole();
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setStatus(status);
        adminRoleMapper.insert(role);
        return requireSummary(role.getId());
    }

    @Transactional
    public AdminRoleSummaryResponse updateRole(Long operatorId, Long roleId, AdminRoleUpsertRequest request) {
        OperatorPermission permission = requireRoleManager(operatorId);
        if (request == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "角色参数不能为空");
        }

        AdminRole role = requireRole(roleId);
        requireRoleEditable(permission, role);

        String roleName = request.getRoleName() == null ? "" : request.getRoleName().trim();
        validateRoleName(roleName);

        adminRoleMapper.update(null, new LambdaUpdateWrapper<AdminRole>()
                .eq(AdminRole::getId, roleId)
                .set(AdminRole::getRoleName, roleName));
        return requireSummary(roleId);
    }

    @Transactional
    public AdminRoleSummaryResponse updateRoleStatus(Long operatorId, Long roleId, AdminRoleStatusUpdateRequest request) {
        OperatorPermission permission = requireRoleManager(operatorId);
        if (request == null || request.getStatus() == null) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "角色状态不能为空");
        }

        validateStatus(request.getStatus());
        AdminRole role = requireRole(roleId);
        requireRoleStatusEditable(permission, role, request.getStatus());

        adminRoleMapper.update(null, new LambdaUpdateWrapper<AdminRole>()
                .eq(AdminRole::getId, roleId)
                .set(AdminRole::getStatus, request.getStatus()));
        return requireSummary(roleId);
    }

    private AdminRoleSummaryResponse requireSummary(Long roleId) {
        return toSummary(requireRole(roleId));
    }

    private AdminRoleSummaryResponse toSummary(AdminRole role) {
        int adminCount = Math.toIntExact(adminUserRoleMapper.selectCount(new LambdaQueryWrapper<AdminUserRole>()
                .eq(AdminUserRole::getRoleId, role.getId())));
        return new AdminRoleSummaryResponse(
                role.getId(),
                role.getRoleCode(),
                role.getRoleName(),
                role.getStatus(),
                adminCount,
                AdminRoleCatalog.descriptionOf(role.getRoleCode()),
                AdminRoleCatalog.capabilitiesOf(role.getRoleCode()),
                AdminRoleCatalog.isProtectedRole(role.getRoleCode()),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    private AdminRole requireRole(Long roleId) {
        AdminRole role = adminRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "角色不存在");
        }
        return role;
    }

    private OperatorPermission requireRoleManager(Long operatorId) {
        Set<String> roleCodes = adminRoleMapper.selectActiveRolesByUserId(operatorId).stream()
                .map(AdminRole::getRoleCode)
                .map(AdminRoleCatalog::normalizeRoleCode)
                .collect(Collectors.toSet());

        boolean isSuperAdmin = roleCodes.contains("SUPER_ADMIN");
        boolean isSystemAdmin = roleCodes.contains("SYSTEM_ADMIN");
        if (!isSuperAdmin && !isSystemAdmin) {
            throw new BusinessException(ApiResponseCode.FORBIDDEN, "当前管理员没有角色管理权限");
        }
        return new OperatorPermission(isSuperAdmin, roleCodes);
    }

    private void requireRoleEditable(OperatorPermission permission, AdminRole role) {
        if (AdminRoleCatalog.isProtectedRole(role.getRoleCode()) && !permission.isSuperAdmin()) {
            throw new BusinessException(ApiResponseCode.FORBIDDEN, "只有超级管理员可以编辑受保护角色");
        }
    }

    private void requireRoleStatusEditable(OperatorPermission permission, AdminRole role, Integer targetStatus) {
        if ("SUPER_ADMIN".equals(AdminRoleCatalog.normalizeRoleCode(role.getRoleCode()))) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "超级管理员角色不允许直接变更状态");
        }
        if (AdminRoleCatalog.isProtectedRole(role.getRoleCode()) && !permission.isSuperAdmin()) {
            throw new BusinessException(ApiResponseCode.FORBIDDEN, "只有超级管理员可以变更受保护角色状态");
        }
        validateStatus(targetStatus);
    }

    private void validateRoleCode(String roleCode) {
        if (!StringUtils.hasText(roleCode)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "角色编码不能为空");
        }
        if (!ROLE_CODE_PATTERN.matcher(roleCode).matches()) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "角色编码需为 2 到 50 位大写字母、数字或下划线，且必须以字母开头");
        }
    }

    private void validateRoleName(String roleName) {
        if (!StringUtils.hasText(roleName)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "角色名称不能为空");
        }
        if (roleName.length() < 2 || roleName.length() > 50) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "角色名称长度需保持在 2 到 50 个字符之间");
        }
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "角色状态仅支持 0 或 1");
        }
    }

    private record OperatorPermission(boolean isSuperAdmin, Set<String> roleCodes) {
    }
}
