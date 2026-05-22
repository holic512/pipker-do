/**
 * @file KyyyReadingPassageMapper
 * @project pipker-do
 * @module 考研英语 / 阅读模块
 * @description 提供阅读文章表的 MyBatis-Plus 读写能力。
 * @logic 1. 读写阅读文章主记录；2. 支撑按英一英二和年份筛选；3. 支撑后台文章维护。
 * @dependencies Entity: KyyyReadingPassage
 * @index_tags 考研英语, 阅读文章Mapper, MyBatisPlus, 文章管理
 * @author holic512
 */
package org.example.backend.biz.kyyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.biz.kyyy.entity.KyyyReadingPassage;

@Mapper
public interface KyyyReadingPassageMapper extends BaseMapper<KyyyReadingPassage> {
}
