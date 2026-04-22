package org.example.backend.shared.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.backend.shared.admin.entity.AdminRole;

import java.util.List;

@Mapper
public interface AdminRoleMapper extends BaseMapper<AdminRole> {

    @Select("""
            SELECT r.*
            FROM admin_role r
            INNER JOIN admin_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId}
              AND r.status = 1
            ORDER BY r.id ASC
            """)
    List<AdminRole> selectActiveRolesByUserId(Long userId);
}
