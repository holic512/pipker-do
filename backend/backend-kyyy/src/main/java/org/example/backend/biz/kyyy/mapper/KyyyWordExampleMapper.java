/**
 * @file KyyyWordExampleMapper
 * @project pipker-do
 * @module 考研英语 / 词库例句
 * @description 提供 KYYY 单词多例句表的 MyBatis-Plus 访问入口。
 * @logic 1. 继承 BaseMapper；2. 支持按 wordId 查询启用例句；3. 供首页查词与背词卡片复用。
 * @dependencies Entity: KyyyWordExample, Table: kyyy_word_example
 * @index_tags 考研英语, 单词例句, Mapper, 多例句
 * @author holic512
 */
package org.example.backend.biz.kyyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.biz.kyyy.entity.KyyyWordExample;

@Mapper
public interface KyyyWordExampleMapper extends BaseMapper<KyyyWordExample> {
}
