-- @file kyyy_writing_essay_migration
-- @project pipker-do
-- @module 考研英语 / 作文知识库
-- @description 新增作文知识库主表，支持归档英一英二历年大小作文内容。
-- @logic 1. 以 writing_code 唯一标识作文；2. 保存题目、范文及双语翻译；3. 为方向、年份、分区等检索建立索引。
-- @dependencies Table: admin_user
-- @index_tags 考研英语, 作文, 知识库, 数据库迁移, 范文
-- @author holic512
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS kyyy_writing_essay (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '作文ID',
    writing_code VARCHAR(80) NOT NULL COMMENT '作文唯一编码',
    exam_direction VARCHAR(20) NOT NULL DEFAULT 'english_one' COMMENT '考试方向：english_one英一/english_two英二',
    source_year SMALLINT NOT NULL COMMENT '来源年份',
    essay_section VARCHAR(20) NOT NULL COMMENT '作文分区：small/big',
    prompt_category VARCHAR(30) NOT NULL DEFAULT 'essay' COMMENT '题目类别：email/letter/notice/chart/picture/essay',
    source_title VARCHAR(120) NOT NULL COMMENT '作文标题',
    score_value INT NOT NULL DEFAULT 0 COMMENT '题目分值',
    word_limit_min INT DEFAULT NULL COMMENT '建议字数下限',
    word_limit_max INT DEFAULT NULL COMMENT '建议字数上限',
    prompt_content TEXT NOT NULL COMMENT '英文题目',
    sample_content MEDIUMTEXT NOT NULL COMMENT '英文范文',
    prompt_translation TEXT COMMENT '中文题目翻译',
    sample_translation MEDIUMTEXT COMMENT '中文范文翻译',
    knowledge_tags VARCHAR(255) DEFAULT NULL COMMENT '检索标签，逗号分隔',
    source_path VARCHAR(255) DEFAULT NULL COMMENT '来源 Markdown 路径',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序值',
    created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建后台管理员ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyyy_writing_essay_code (writing_code),
    KEY idx_kyyy_writing_essay_exam_year_section (exam_direction, source_year, essay_section),
    KEY idx_kyyy_writing_essay_category_status_sort (prompt_category, status, sort_no),
    KEY idx_kyyy_writing_essay_created_by (created_by),
    CONSTRAINT fk_kyyy_writing_essay_created_by FOREIGN KEY (created_by) REFERENCES admin_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYYY作文知识库主表';

SET FOREIGN_KEY_CHECKS = 1;
