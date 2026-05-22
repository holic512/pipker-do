-- @file kyyy_reading_tables_migration
-- @project pipker-do
-- @module 考研英语 / 阅读模块
-- @description 新增阅读文章、阅读题、作答历史、问题反馈与错题聚合表结构。
-- @logic 1. 建立篇章-题目-选项三级关系；2. 建立阅读会话与历史作答快照；3. 建立反馈工单与错题聚合表。
-- @dependencies Table: app_user, Table: admin_user
-- @index_tags 考研英语, 阅读题, 数据库迁移, 作答历史, 错题本
-- @author holic512
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS kyyy_reading_passage (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '阅读文章ID',
    passage_code VARCHAR(50) NOT NULL COMMENT '阅读文章编码',
    exam_direction VARCHAR(20) NOT NULL DEFAULT 'english_one' COMMENT '考试方向：english_one英一/english_two英二',
    source_year SMALLINT DEFAULT NULL COMMENT '来源年份',
    source_name VARCHAR(100) DEFAULT NULL COMMENT '来源名称',
    passage_no INT NOT NULL DEFAULT 1 COMMENT '卷内篇次，如Text 1为1',
    title VARCHAR(255) DEFAULT NULL COMMENT '文章标题',
    passage_text TEXT NOT NULL COMMENT '阅读文章全文',
    question_count INT NOT NULL DEFAULT 0 COMMENT '文章题目数',
    total_score DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '文章总分',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序值',
    created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建后台管理员ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyyy_reading_passage_code (passage_code),
    KEY idx_kyyy_reading_passage_exam_year_status_sort (exam_direction, source_year, status, sort_no),
    KEY idx_kyyy_reading_passage_status_sort (status, sort_no),
    KEY idx_kyyy_reading_passage_created_by (created_by),
    CONSTRAINT fk_kyyy_reading_passage_created_by FOREIGN KEY (created_by) REFERENCES admin_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYYY阅读文章表';

CREATE TABLE IF NOT EXISTS kyyy_reading_question (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '阅读题目ID',
    passage_id BIGINT UNSIGNED NOT NULL COMMENT '所属阅读文章ID',
    question_no INT NOT NULL COMMENT '文章内题号',
    question_type VARCHAR(30) NOT NULL DEFAULT 'single_choice' COMMENT '题型：single_choice',
    stem TEXT NOT NULL COMMENT '题干',
    answer_text VARCHAR(100) DEFAULT NULL COMMENT '标准答案文本',
    analysis TEXT COMMENT '题目解析',
    difficulty_level TINYINT NOT NULL DEFAULT 2 COMMENT '难度：1简单 2中等 3困难',
    score DECIMAL(6,2) NOT NULL DEFAULT 1.00 COMMENT '分值',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序值',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyyy_reading_question_passage_no (passage_id, question_no),
    KEY idx_kyyy_reading_question_passage_status_sort (passage_id, status, sort_no),
    KEY idx_kyyy_reading_question_type_status (question_type, status),
    CONSTRAINT fk_kyyy_reading_question_passage_id FOREIGN KEY (passage_id) REFERENCES kyyy_reading_passage (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYYY阅读题目表';

CREATE TABLE IF NOT EXISTS kyyy_reading_question_option (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '阅读题选项ID',
    question_id BIGINT UNSIGNED NOT NULL COMMENT '阅读题目ID',
    option_key VARCHAR(10) NOT NULL COMMENT '选项标识：A/B/C/D',
    option_content TEXT NOT NULL COMMENT '选项内容',
    is_correct TINYINT NOT NULL DEFAULT 0 COMMENT '是否正确：0否 1是',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序值',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyyy_reading_question_option_question_key (question_id, option_key),
    CONSTRAINT fk_kyyy_reading_question_option_question_id FOREIGN KEY (question_id) REFERENCES kyyy_reading_question (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYYY阅读题选项表';

CREATE TABLE IF NOT EXISTS kyyy_reading_session (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '阅读练习会话ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    passage_id BIGINT UNSIGNED NOT NULL COMMENT '阅读文章ID',
    exam_direction_snapshot VARCHAR(20) NOT NULL COMMENT '作答时考试方向快照',
    session_status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '会话状态：active/submitted/abandoned',
    total_question_count INT NOT NULL DEFAULT 0 COMMENT '总题数',
    answered_count INT NOT NULL DEFAULT 0 COMMENT '已作答题数',
    correct_count INT NOT NULL DEFAULT 0 COMMENT '答对题数',
    wrong_count INT NOT NULL DEFAULT 0 COMMENT '答错题数',
    accuracy_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '正确率百分比',
    started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    submitted_at DATETIME DEFAULT NULL COMMENT '提交时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_kyyy_reading_session_user_status_started_at (user_id, session_status, started_at),
    KEY idx_kyyy_reading_session_user_passage_started_at (user_id, passage_id, started_at),
    KEY idx_kyyy_reading_session_passage_started_at (passage_id, started_at),
    CONSTRAINT fk_kyyy_reading_session_user_id FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_kyyy_reading_session_passage_id FOREIGN KEY (passage_id) REFERENCES kyyy_reading_passage (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYYY阅读练习会话表';

CREATE TABLE IF NOT EXISTS kyyy_user_reading_answer (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户阅读答题记录ID',
    session_id BIGINT UNSIGNED NOT NULL COMMENT '阅读练习会话ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    passage_id BIGINT UNSIGNED NOT NULL COMMENT '阅读文章ID',
    question_id BIGINT UNSIGNED NOT NULL COMMENT '阅读题目ID',
    question_no_snapshot INT NOT NULL COMMENT '提交时题号快照',
    question_stem_snapshot TEXT COMMENT '提交时题干快照',
    option_snapshot_json TEXT COMMENT '提交时选项快照JSON',
    answer_content VARCHAR(100) DEFAULT NULL COMMENT '用户答案',
    correct_answer_snapshot VARCHAR(100) DEFAULT NULL COMMENT '提交时标准答案快照',
    analysis_snapshot TEXT COMMENT '提交时解析快照',
    is_correct TINYINT NOT NULL DEFAULT 0 COMMENT '是否答对：0否 1是',
    answer_status VARCHAR(20) NOT NULL DEFAULT 'answered' COMMENT '作答状态：answered/skipped',
    used_seconds INT NOT NULL DEFAULT 0 COMMENT '耗时秒数',
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyyy_user_reading_answer_session_question (session_id, question_id),
    KEY idx_kyyy_user_reading_answer_user_question_submitted_at (user_id, question_id, submitted_at),
    KEY idx_kyyy_user_reading_answer_user_passage_submitted_at (user_id, passage_id, submitted_at),
    KEY idx_kyyy_user_reading_answer_question_submitted_at (question_id, submitted_at),
    CONSTRAINT fk_kyyy_user_reading_answer_session_id FOREIGN KEY (session_id) REFERENCES kyyy_reading_session (id),
    CONSTRAINT fk_kyyy_user_reading_answer_user_id FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_kyyy_user_reading_answer_passage_id FOREIGN KEY (passage_id) REFERENCES kyyy_reading_passage (id),
    CONSTRAINT fk_kyyy_user_reading_answer_question_id FOREIGN KEY (question_id) REFERENCES kyyy_reading_question (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYYY用户阅读答题记录表';

CREATE TABLE IF NOT EXISTS kyyy_reading_feedback (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '阅读问题反馈ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    passage_id BIGINT UNSIGNED NOT NULL COMMENT '阅读文章ID',
    question_id BIGINT UNSIGNED DEFAULT NULL COMMENT '阅读题目ID，为空表示整篇反馈',
    session_id BIGINT UNSIGNED DEFAULT NULL COMMENT '阅读练习会话ID',
    feedback_type VARCHAR(30) NOT NULL DEFAULT 'other' COMMENT '反馈类型：passage_text_error/question_stem_error/option_error/answer_error/analysis_error/other',
    content TEXT NOT NULL COMMENT '反馈内容',
    contact_info VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '处理状态：pending/processing/resolved/rejected',
    admin_reply TEXT COMMENT '后台处理回复',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_kyyy_reading_feedback_status_created_at (status, created_at),
    KEY idx_kyyy_reading_feedback_passage_question_created_at (passage_id, question_id, created_at),
    KEY idx_kyyy_reading_feedback_user_created_at (user_id, created_at),
    CONSTRAINT fk_kyyy_reading_feedback_user_id FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_kyyy_reading_feedback_passage_id FOREIGN KEY (passage_id) REFERENCES kyyy_reading_passage (id),
    CONSTRAINT fk_kyyy_reading_feedback_question_id FOREIGN KEY (question_id) REFERENCES kyyy_reading_question (id),
    CONSTRAINT fk_kyyy_reading_feedback_session_id FOREIGN KEY (session_id) REFERENCES kyyy_reading_session (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYYY阅读问题反馈表';

CREATE TABLE IF NOT EXISTS kyyy_user_reading_wrong_question (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户阅读错题ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    passage_id BIGINT UNSIGNED NOT NULL COMMENT '阅读文章ID',
    question_id BIGINT UNSIGNED NOT NULL COMMENT '阅读题目ID',
    first_wrong_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次错题时间',
    last_wrong_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近错题时间',
    wrong_count INT NOT NULL DEFAULT 1 COMMENT '累计错题次数',
    is_mastered TINYINT NOT NULL DEFAULT 0 COMMENT '是否掌握：0否 1是',
    mastered_at DATETIME DEFAULT NULL COMMENT '掌握时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyyy_user_reading_wrong_question_user_question (user_id, question_id),
    KEY idx_kyyy_user_reading_wrong_question_user_mastered_last_wrong_at (user_id, is_mastered, last_wrong_at),
    KEY idx_kyyy_user_reading_wrong_question_passage_mastered (passage_id, is_mastered),
    CONSTRAINT fk_kyyy_user_reading_wrong_question_user_id FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_kyyy_user_reading_wrong_question_passage_id FOREIGN KEY (passage_id) REFERENCES kyyy_reading_passage (id),
    CONSTRAINT fk_kyyy_user_reading_wrong_question_question_id FOREIGN KEY (question_id) REFERENCES kyyy_reading_question (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYYY用户阅读错题表';

SET FOREIGN_KEY_CHECKS = 1;
