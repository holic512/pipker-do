package org.example.backend.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.common.entiy.AppUser;

@Mapper
public interface AppUserMapper extends BaseMapper<AppUser> {
}
