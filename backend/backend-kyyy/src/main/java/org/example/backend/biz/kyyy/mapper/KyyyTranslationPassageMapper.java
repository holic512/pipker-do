/**
 * @file KyyyTranslationPassageMapper
 * @project pipker-do
 * @module 考研英语 / 翻译知识库
 * @description 提供翻译知识库主表的基础数据访问能力。
 * @logic 1. 复用 MyBatis-Plus BaseMapper；2. 为翻译检索和详情查询提供持久化入口；3. 与翻译主表实体保持一一对应。
 * @dependencies Entity: KyyyTranslationPassage
 * @index_tags 考研英语, 翻译, Mapper, MyBatis-Plus, 知识库
 * @author holic512
 */
package org.example.backend.biz.kyyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.biz.kyyy.entity.KyyyTranslationPassage;

@Mapper
public interface KyyyTranslationPassageMapper extends BaseMapper<KyyyTranslationPassage> {
}
