/**
 * @file KyyyWritingEssayMapper
 * @project pipker-do
 * @module 考研英语 / 作文知识库
 * @description 提供作文知识库主表的基础数据访问能力。
 * @logic 1. 复用 MyBatis-Plus BaseMapper；2. 为作文检索与后续管理接口提供持久化入口；3. 与作文主表实体保持一一对应。
 * @dependencies Entity: KyyyWritingEssay
 * @index_tags 考研英语, 作文, Mapper, MyBatis-Plus, 知识库
 * @author holic512
 */
package org.example.backend.biz.kyyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.biz.kyyy.entity.KyyyWritingEssay;

@Mapper
public interface KyyyWritingEssayMapper extends BaseMapper<KyyyWritingEssay> {
}
