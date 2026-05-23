/**
 * @file KyyyTranslationSegmentMapper
 * @project pipker-do
 * @module 考研英语 / 翻译知识库
 * @description 提供翻译知识库分段表的基础数据访问能力。
 * @logic 1. 复用 MyBatis-Plus BaseMapper；2. 为分段详情查询提供持久化入口；3. 与翻译分段实体保持一一对应。
 * @dependencies Entity: KyyyTranslationSegment
 * @index_tags 考研英语, 翻译分段, Mapper, MyBatis-Plus, 知识库
 * @author holic512
 */
package org.example.backend.biz.kyyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.biz.kyyy.entity.KyyyTranslationSegment;

@Mapper
public interface KyyyTranslationSegmentMapper extends BaseMapper<KyyyTranslationSegment> {
}
