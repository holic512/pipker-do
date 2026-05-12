/**
 * @file KyyyWordPracticeSessionMapper
 * @project pipker-do
 * @module 考研英语 / 背词会话
 * @description 提供背词会话主表的 MyBatis-Plus 读写能力。
 * @logic 1. 读写会话主记录；2. 支撑会话恢复、完成与废弃；3. 供服务层更新进度统计。
 * @dependencies Entity: KyyyWordPracticeSession
 * @index_tags 考研英语, 会话Mapper, 背词会话, MyBatisPlus
 * @author holic512
 */
package org.example.backend.biz.kyyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.biz.kyyy.entity.KyyyWordPracticeSession;

@Mapper
public interface KyyyWordPracticeSessionMapper extends BaseMapper<KyyyWordPracticeSession> {
}
