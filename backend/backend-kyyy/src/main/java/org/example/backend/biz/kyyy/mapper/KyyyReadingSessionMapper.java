/**
 * @file KyyyReadingSessionMapper
 * @project pipker-do
 * @module 考研英语 / 阅读模块
 * @description 提供阅读练习会话表的 MyBatis-Plus 读写能力。
 * @logic 1. 读写单次阅读练习会话；2. 支撑历史列表查询；3. 支撑会话统计更新。
 * @dependencies Entity: KyyyReadingSession
 * @index_tags 考研英语, 阅读会话Mapper, MyBatisPlus, 历史练习
 * @author holic512
 */
package org.example.backend.biz.kyyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.biz.kyyy.entity.KyyyReadingSession;

@Mapper
public interface KyyyReadingSessionMapper extends BaseMapper<KyyyReadingSession> {
}
