package org.example.backend.biz.kyzz.admin.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.admin.entity.AdminProjectAccess;
import org.example.backend.shared.admin.entity.AdminUser;
import org.example.backend.shared.admin.mapper.AdminProjectAccessMapper;
import org.example.backend.shared.admin.mapper.AdminUserMapper;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * AI 索引: KYZZ 后台权限校验。
 */
@Component
public class KyzzAdminAccessSupport {

    private static final String PROJECT_CODE = "kyzz";

    private final AdminProjectAccessMapper adminProjectAccessMapper;
    private final AdminUserMapper adminUserMapper;

    public KyzzAdminAccessSupport(AdminProjectAccessMapper adminProjectAccessMapper,
                                  AdminUserMapper adminUserMapper) {
        this.adminProjectAccessMapper = adminProjectAccessMapper;
        this.adminUserMapper = adminUserMapper;
    }

    public void requireProjectAccess(Long operatorId) {
        AdminUser adminUser = adminUserMapper.selectById(operatorId);
        if (adminUser == null || !Objects.equals(adminUser.getStatus(), 1)) {
            throw new BusinessException(ApiResponseCode.FORBIDDEN, "当前管理员不可用");
        }

        long projectAccess = adminProjectAccessMapper.selectCount(new LambdaQueryWrapper<AdminProjectAccess>()
                .eq(AdminProjectAccess::getUserId, operatorId)
                .eq(AdminProjectAccess::getProjectCode, PROJECT_CODE)
                .eq(AdminProjectAccess::getEnabled, 1));
        if (projectAccess <= 0) {
            throw new BusinessException(ApiResponseCode.FORBIDDEN, "当前管理员没有考研政治项目权限");
        }
    }
}
