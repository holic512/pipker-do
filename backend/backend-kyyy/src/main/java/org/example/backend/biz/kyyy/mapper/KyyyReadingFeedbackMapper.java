/**
 * @file KyyyReadingFeedbackMapper
 * @project pipker-do
 * @module 考研英语 / 阅读模块
 * @description 提供阅读问题反馈表的 MyBatis-Plus 读写能力。
 * @logic 1. 读写阅读问题反馈工单；2. 支撑按状态和用户查询；3. 支撑后台处理回复回写。
 * @dependencies Entity: KyyyReadingFeedback
 * @index_tags 考研英语, 阅读反馈Mapper, MyBatisPlus, 工单处理
 * @author holic512
 */
package org.example.backend.biz.kyyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.biz.kyyy.entity.KyyyReadingFeedback;

@Mapper
public interface KyyyReadingFeedbackMapper extends BaseMapper<KyyyReadingFeedback> {
}
