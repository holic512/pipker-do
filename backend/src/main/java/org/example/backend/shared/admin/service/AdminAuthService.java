package org.example.backend.shared.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.admin.dto.AdminCurrentUserResponse;
import org.example.backend.shared.admin.dto.AdminLoginResponse;
import org.example.backend.shared.admin.dto.AdminProjectResponse;
import org.example.backend.shared.admin.entity.AdminProjectAccess;
import org.example.backend.shared.admin.entity.AdminRole;
import org.example.backend.shared.admin.entity.AdminUser;
import org.example.backend.shared.admin.mapper.AdminProjectAccessMapper;
import org.example.backend.shared.admin.mapper.AdminRoleMapper;
import org.example.backend.shared.admin.mapper.AdminUserMapper;
import org.example.backend.shared.admin.support.AdminProjectCatalog;
import org.example.backend.shared.security.StpKit;
import org.example.backend.util.password.SCryptUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminAuthService {

    private final AdminUserMapper adminUserMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final AdminProjectAccessMapper adminProjectAccessMapper;

    public AdminAuthService(AdminUserMapper adminUserMapper,
                            AdminRoleMapper adminRoleMapper,
                            AdminProjectAccessMapper adminProjectAccessMapper) {
        this.adminUserMapper = adminUserMapper;
        this.adminRoleMapper = adminRoleMapper;
        this.adminProjectAccessMapper = adminProjectAccessMapper;
    }

    @Transactional
    public AdminLoginResponse login(String username, String password) {
        AdminUser adminUser = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, username)
                .last("limit 1"));
        if (adminUser == null || !StringUtils.hasText(adminUser.getPasswordHash())
                || !SCryptUtil.verifyPassword(password, adminUser.getPasswordHash())) {
            throw new BusinessException(ApiResponseCode.UNAUTHORIZED, "账号或密码错误");
        }
        if (adminUser.getStatus() == null || adminUser.getStatus() != 1) {
            throw new BusinessException(ApiResponseCode.FORBIDDEN, "当前管理员账号已被禁用");
        }

        adminUserMapper.update(null, new LambdaUpdateWrapper<AdminUser>()
                .eq(AdminUser::getId, adminUser.getId())
                .set(AdminUser::getLastLoginAt, LocalDateTime.now()));
        adminUser.setLastLoginAt(LocalDateTime.now());

        StpKit.ADMIN.login(adminUser.getId());
        String token = StpKit.ADMIN.getTokenValue();
        return new AdminLoginResponse(token, buildCurrentAdmin(adminUser.getId()));
    }

    public void logout() {
        StpKit.ADMIN.logout();
    }

    public AdminCurrentUserResponse buildCurrentAdmin(Long adminId) {
        AdminUser adminUser = requireAdmin(adminId);
        List<AdminProjectResponse> projects = getAvailableProjects(adminId);
        if (projects.isEmpty()) {
            throw new BusinessException(ApiResponseCode.FORBIDDEN, "当前管理员未分配任何项目权限");
        }

        List<String> roles = adminRoleMapper.selectActiveRolesByUserId(adminId).stream()
                .map(AdminRole::getRoleCode)
                .distinct()
                .toList();
        String defaultProjectCode = resolveDefaultProjectCode(adminUser.getDefaultProjectCode(), projects);

        return new AdminCurrentUserResponse(
                adminUser.getId(),
                adminUser.getUsername(),
                adminUser.getDisplayName(),
                roles,
                projects,
                defaultProjectCode
        );
    }

    public List<AdminProjectResponse> getAvailableProjects(Long adminId) {
        return adminProjectAccessMapper.selectList(new LambdaQueryWrapper<AdminProjectAccess>()
                        .eq(AdminProjectAccess::getUserId, adminId)
                        .eq(AdminProjectAccess::getEnabled, 1)
                        .orderByAsc(AdminProjectAccess::getId))
                .stream()
                .map(AdminProjectAccess::getProjectCode)
                .filter(AdminProjectCatalog::supports)
                .distinct()
                .map(projectCode -> AdminProjectCatalog.toProject(projectCode, true))
                .toList();
    }

    private String resolveDefaultProjectCode(String savedProjectCode, List<AdminProjectResponse> projects) {
        if (StringUtils.hasText(savedProjectCode) && projects.stream().anyMatch(project -> savedProjectCode.equals(project.getCode()))) {
            return savedProjectCode;
        }
        return projects.get(0).getCode();
    }

    private AdminUser requireAdmin(Long adminId) {
        AdminUser adminUser = adminUserMapper.selectById(adminId);
        if (adminUser == null) {
            throw new BusinessException(ApiResponseCode.NOT_FOUND, "管理员不存在");
        }
        if (adminUser.getStatus() == null || adminUser.getStatus() != 1) {
            throw new BusinessException(ApiResponseCode.FORBIDDEN, "当前管理员账号已被禁用");
        }
        return adminUser;
    }
}
