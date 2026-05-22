/**
 * @file KyyyReadingQuestionOptionMapper
 * @project pipker-do
 * @module 考研英语 / 阅读模块
 * @description 提供阅读题选项表的 MyBatis-Plus 读写能力。
 * @logic 1. 读写题目选项；2. 支撑选项顺序维护；3. 支撑标准答案校验。
 * @dependencies Entity: KyyyReadingQuestionOption
 * @index_tags 考研英语, 阅读选项Mapper, MyBatisPlus, 题目选项
 * @author holic512
 */
package org.example.backend.biz.kyyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.biz.kyyy.entity.KyyyReadingQuestionOption;

@Mapper
public interface KyyyReadingQuestionOptionMapper extends BaseMapper<KyyyReadingQuestionOption> {
}
