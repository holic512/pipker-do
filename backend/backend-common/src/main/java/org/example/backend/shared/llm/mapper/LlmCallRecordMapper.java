package org.example.backend.shared.llm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.shared.llm.entity.LlmCallRecord;

/**
 * AI 索引: LLM 调用记录 mapper。
 */
@Mapper
public interface LlmCallRecordMapper extends BaseMapper<LlmCallRecord> {
}
