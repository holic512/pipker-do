/**
 * @file KyyyUserReadingAnswerMapper
 * @project pipker-do
 * @module 考研英语 / 阅读模块
 * @description 提供用户阅读历史作答表的 MyBatis-Plus 读写能力。
 * @logic 1. 读写会话内答题明细；2. 支撑按用户、文章和题目检索历史；3. 支撑快照回放。
 * @dependencies Entity: KyyyUserReadingAnswer
 * @index_tags 考研英语, 阅读答题记录Mapper, MyBatisPlus, 历史作答
 * @author holic512
 */
package org.example.backend.biz.kyyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.biz.kyyy.entity.KyyyUserReadingAnswer;

@Mapper
public interface KyyyUserReadingAnswerMapper extends BaseMapper<KyyyUserReadingAnswer> {
}
