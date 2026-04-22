package org.example.backend.shared.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.shared.admin.entity.AdminUser;

@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
}
