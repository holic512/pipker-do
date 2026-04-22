package org.example.backend.shared.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.admin.dto.AdminCurrentUserResponse;
import org.example.backend.shared.admin.dto.AdminLoginResponse;
import org.example.backend.shared.admin.dto.AdminPasswordUpdateRequest;
import org.example.backend.shared.admin.dto.AdminProjectResponse;
import org.example.backend.shared.admin.dto.AdminProfileUpdateRequest;
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
                defaultProjectCode,
                adminUser.getLastLoginAt()
        );
    }

    @Transactional
    public AdminCurrentUserResponse updateProfile(Long adminId, AdminProfileUpdateRequest request) {
        AdminUser adminUser = requireAdmin(adminId);
        String displayName = request.getDisplayName() == null ? "" : request.getDisplayName().trim();
        if (!StringUtils.hasText(displayName)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "显示名称不能为空");
        }
        if (displayName.length() > 50) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "显示名称不能超过50个字符");
        }

        List<AdminProjectResponse> projects = getAvailableProjects(adminId);
        if (projects.isEmpty()) {
            throw new BusinessException(ApiResponseCode.FORBIDDEN, "当前管理员未分配任何项目权限");
        }

        String defaultProjectCode = request.getDefaultProjectCode() == null ? "" : request.getDefaultProjectCode().trim();
        if (!StringUtils.hasText(defaultProjectCode)) {
            defaultProjectCode = resolveDefaultProjectCode(adminUser.getDefaultProjectCode(), projects);
        }
        final String finalDefaultProjectCode = defaultProjectCode;
        boolean hasProjectAccess = projects.stream().anyMatch(project -> finalDefaultProjectCode.equals(project.getCode()));
        if (!hasProjectAccess) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "默认项目必须是当前账号有权限访问的项目");
        }

        adminUserMapper.update(null, new LambdaUpdateWrapper<AdminUser>()
                .eq(AdminUser::getId, adminId)
                .set(AdminUser::getDisplayName, displayName)
                .set(AdminUser::getDefaultProjectCode, defaultProjectCode));

        return buildCurrentAdmin(adminId);
    }

    @Transactional
    public void updatePassword(Long adminId, AdminPasswordUpdateRequest request) {
        AdminUser adminUser = requireAdmin(adminId);
        String currentPassword = request.getCurrentPassword() == null ? "" : request.getCurrentPassword();
        String newPassword = request.getNewPassword() == null ? "" : request.getNewPassword();
        String confirmPassword = request.getConfirmPassword() == null ? "" : request.getConfirmPassword();

        if (!StringUtils.hasText(currentPassword) || !StringUtils.hasText(newPassword) || !StringUtils.hasText(confirmPassword)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "请完整填写当前密码、新密码和确认密码");
        }
        if (!StringUtils.hasText(adminUser.getPasswordHash()) || !SCryptUtil.verifyPassword(currentPassword, adminUser.getPasswordHash())) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "当前密码不正确");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "两次输入的新密码不一致");
        }
        if (currentPassword.equals(newPassword)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "新密码不能与当前密码相同");
        }

        validateNewPassword(newPassword, adminUser.getUsername());

        adminUserMapper.update(null, new LambdaUpdateWrapper<AdminUser>()
                .eq(AdminUser::getId, adminId)
                .set(AdminUser::getPasswordHash, SCryptUtil.hashPassword(newPassword)));
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

    private void validateNewPassword(String password, String username) {
        if (password.length() < 8 || password.length() > 32) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "新密码长度需保持在8到32位之间");
        }
        if (StringUtils.hasText(username) && password.toLowerCase().contains(username.toLowerCase())) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "新密码不能包含账号名");
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
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "新密码需至少包含大写字母、小写字母、数字、符号中的3类");
        }
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
