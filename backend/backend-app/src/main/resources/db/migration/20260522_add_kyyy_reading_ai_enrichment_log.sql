-- @file kyyy_reading_ai_enrichment_log_migration
-- @project pipker-do
-- @module 考研英语 / 阅读模块
-- @description 新增阅读 AI 补齐日志表，用于记录答案与解析补齐过程。
-- @logic 1. 记录题目与篇章快照；2. 记录模型、答案、置信度与 token 消耗；3. 记录失败与校验异常原因。
-- @dependencies Table: kyyy_reading_passage, Table: kyyy_reading_question
-- @index_tags 考研英语, 阅读补齐, AI日志, 数据库迁移
-- @author holic512
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS kyyy_reading_ai_enrichment_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '阅读AI补齐日志ID',
    run_id VARCHAR(64) NOT NULL COMMENT '运行ID',
    question_id BIGINT UNSIGNED DEFAULT NULL COMMENT '阅读题目ID',
    passage_id BIGINT UNSIGNED DEFAULT NULL COMMENT '阅读文章ID',
    passage_code VARCHAR(50) DEFAULT NULL COMMENT '阅读文章编码快照',
    exam_direction VARCHAR(20) DEFAULT NULL COMMENT '考试方向快照',
    source_year SMALLINT DEFAULT NULL COMMENT '来源年份快照',
    question_no INT DEFAULT NULL COMMENT '题号快照',
    status VARCHAR(30) NOT NULL COMMENT '状态：skipped/success/failed/validation_failed',
    model VARCHAR(80) DEFAULT NULL COMMENT '调用模型',
    prompt_hash CHAR(64) DEFAULT NULL COMMENT 'Prompt SHA-256',
    response_hash CHAR(64) DEFAULT NULL COMMENT '响应 SHA-256',
    answer_key VARCHAR(20) DEFAULT NULL COMMENT 'AI返回答案',
    confidence DECIMAL(5,4) DEFAULT NULL COMMENT 'AI置信度',
    analysis_length INT NOT NULL DEFAULT 0 COMMENT '解析长度',
    input_tokens INT DEFAULT NULL COMMENT '输入token数',
    output_tokens INT DEFAULT NULL COMMENT '输出token数',
    total_tokens INT DEFAULT NULL COMMENT '总token数',
    error_message VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_kyyy_reading_ai_enrichment_log_run_id (run_id),
    KEY idx_kyyy_reading_ai_enrichment_log_question_status (question_id, status),
    KEY idx_kyyy_reading_ai_enrichment_log_passage_status (passage_id, status),
    CONSTRAINT fk_kyyy_reading_ai_enrichment_log_question_id FOREIGN KEY (question_id) REFERENCES kyyy_reading_question (id),
    CONSTRAINT fk_kyyy_reading_ai_enrichment_log_passage_id FOREIGN KEY (passage_id) REFERENCES kyyy_reading_passage (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYYY阅读AI补齐日志表';

SET FOREIGN_KEY_CHECKS = 1;
