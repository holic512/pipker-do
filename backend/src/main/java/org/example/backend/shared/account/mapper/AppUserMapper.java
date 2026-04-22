package org.example.backend.shared.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.shared.account.entity.AppUser;

@Mapper
public interface AppUserMapper extends BaseMapper<AppUser> {
}
