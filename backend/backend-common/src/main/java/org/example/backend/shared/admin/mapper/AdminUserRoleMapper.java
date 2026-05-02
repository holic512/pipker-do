package org.example.backend.shared.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.backend.shared.admin.entity.AdminUserRole;

@Mapper
public interface AdminUserRoleMapper extends BaseMapper<AdminUserRole> {

    @Select("""
            SELECT COUNT(DISTINCT u.id)
            FROM admin_user_role ur
            INNER JOIN admin_role r ON r.id = ur.role_id
            INNER JOIN admin_user u ON u.id = ur.user_id
            WHERE r.role_code = #{roleCode}
              AND r.status = 1
              AND u.status = 1
            """)
    long countActiveUsersByRoleCode(String roleCode);
}
