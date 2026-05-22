/**
 * @file KyyyUserReadingAnnotationMapper
 * @project pipker-do
 * @module 考研英语 / 阅读标注
 * @description 提供用户阅读标注表的 MyBatis-Plus 读写能力。
 * @logic 1. 读写正文与题干标注；2. 支撑区间冲突检测；3. 支撑会话恢复时的标注读取。
 * @dependencies Entity: KyyyUserReadingAnnotation
 * @index_tags 考研英语, 阅读标注Mapper, MyBatisPlus, 高亮备注
 * @author holic512
 */
package org.example.backend.biz.kyyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.biz.kyyy.entity.KyyyUserReadingAnnotation;

@Mapper
public interface KyyyUserReadingAnnotationMapper extends BaseMapper<KyyyUserReadingAnnotation> {
}
