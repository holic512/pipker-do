/**
 * @file KyyyWordPracticeSessionItemMapper
 * @project pipker-do
 * @module 考研英语 / 背词会话
 * @description 提供背词会话出卡记录表的 MyBatis-Plus 读写能力。
 * @logic 1. 读写会话卡片记录；2. 支撑队列恢复与回插排序；3. 支撑会话结果统计。
 * @dependencies Entity: KyyyWordPracticeSessionItem
 * @index_tags 考研英语, 会话项Mapper, 背词卡片, MyBatisPlus
 * @author holic512
 */
package org.example.backend.biz.kyyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.biz.kyyy.entity.KyyyWordPracticeSessionItem;

@Mapper
public interface KyyyWordPracticeSessionItemMapper extends BaseMapper<KyyyWordPracticeSessionItem> {
}
