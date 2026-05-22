/**
 * @file KyyyReadingQuestionMapper
 * @project pipker-do
 * @module 考研英语 / 阅读模块
 * @description 提供阅读题目表的 MyBatis-Plus 读写能力。
 * @logic 1. 读写篇内题目；2. 支撑题号排序与状态筛选；3. 支撑答案和解析维护。
 * @dependencies Entity: KyyyReadingQuestion
 * @index_tags 考研英语, 阅读题目Mapper, MyBatisPlus, 小题管理
 * @author holic512
 */
package org.example.backend.biz.kyyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.biz.kyyy.entity.KyyyReadingQuestion;

@Mapper
public interface KyyyReadingQuestionMapper extends BaseMapper<KyyyReadingQuestion> {
}
