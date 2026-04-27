package org.example.backend.shared.admin.service;

import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.admin.entity.AdminRole;
import org.example.backend.shared.admin.mapper.AdminRoleMapper;
import org.example.backend.shared.admin.support.AdminRoleCatalog;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI 索引: 后台通用角色权限校验服务。
 */
@Service
public class AdminPermissionService {

    private final AdminRoleMapper adminRoleMapper;

    public AdminPermissionService(AdminRoleMapper adminRoleMapper) {
        this.adminRoleMapper = adminRoleMapper;
    }

    public void requireSystemManager(Long operatorId) {
        Set<String> roleCodes = adminRoleMapper.selectActiveRolesByUserId(operatorId).stream()
                .map(AdminRole::getRoleCode)
                .map(AdminRoleCatalog::normalizeRoleCode)
                .collect(Collectors.toSet());
        if (!roleCodes.contains("SUPER_ADMIN") && !roleCodes.contains("SYSTEM_ADMIN")) {
            throw new BusinessException(ApiResponseCode.FORBIDDEN, "当前管理员没有系统配置管理权限");
        }
    }
}
