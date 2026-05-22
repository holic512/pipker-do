/**
 * @file KyyyUserReadingWrongQuestionMapper
 * @project pipker-do
 * @module 考研英语 / 阅读模块
 * @description 提供用户阅读错题聚合表的 MyBatis-Plus 读写能力。
 * @logic 1. 读写阅读错题聚合记录；2. 支撑错题本列表查询；3. 支撑掌握状态更新。
 * @dependencies Entity: KyyyUserReadingWrongQuestion
 * @index_tags 考研英语, 阅读错题Mapper, MyBatisPlus, 错题本
 * @author holic512
 */
package org.example.backend.biz.kyyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.biz.kyyy.entity.KyyyUserReadingWrongQuestion;

@Mapper
public interface KyyyUserReadingWrongQuestionMapper extends BaseMapper<KyyyUserReadingWrongQuestion> {
}
