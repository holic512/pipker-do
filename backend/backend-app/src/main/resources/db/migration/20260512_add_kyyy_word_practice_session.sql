SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE kyyy_user_word_progress
    MODIFY COLUMN study_status VARCHAR(20) NOT NULL DEFAULT 'new' COMMENT '学习状态：new/learning/review/relearning/mastered',
    ADD COLUMN memory_stage TINYINT NOT NULL DEFAULT 0 COMMENT '长期记忆阶段：0-5' AFTER mastery_level,
    ADD COLUMN learning_step TINYINT NOT NULL DEFAULT 0 COMMENT '短期学习步进：0未开始 1首轮通过 2已毕业' AFTER memory_stage,
    ADD COLUMN lapse_count INT NOT NULL DEFAULT 0 COMMENT '遗忘次数' AFTER learning_step,
    ADD COLUMN consecutive_known_count INT NOT NULL DEFAULT 0 COMMENT '连续认识次数' AFTER lapse_count,
    MODIFY COLUMN last_result VARCHAR(20) DEFAULT NULL COMMENT '最近结果：know/fuzzy/unknown';

UPDATE kyyy_user_word_progress
SET last_result = CASE
    WHEN last_result = 'correct' THEN 'know'
    WHEN last_result = 'wrong' THEN 'unknown'
    WHEN last_result = 'skip' THEN 'fuzzy'
    ELSE last_result
END
WHERE last_result IN ('correct', 'wrong', 'skip');

CREATE TABLE IF NOT EXISTS kyyy_word_practice_session (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '背词会话ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    word_bank_id BIGINT UNSIGNED NOT NULL COMMENT '词库ID',
    mode VARCHAR(20) NOT NULL DEFAULT 'study' COMMENT '会话模式：study/review',
    status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '会话状态：active/completed/abandoned',
    total_cards INT NOT NULL DEFAULT 20 COMMENT '会话预算卡片数',
    completed_cards INT NOT NULL DEFAULT 0 COMMENT '已完成卡片数',
    known_count INT NOT NULL DEFAULT 0 COMMENT '认识次数',
    fuzzy_count INT NOT NULL DEFAULT 0 COMMENT '模糊次数',
    unknown_count INT NOT NULL DEFAULT 0 COMMENT '不认识次数',
    started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    last_answered_at DATETIME DEFAULT NULL COMMENT '最近答题时间',
    finished_at DATETIME DEFAULT NULL COMMENT '完成时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_kyyy_word_practice_session_user_bank_mode_status (user_id, word_bank_id, mode, status),
    KEY idx_kyyy_word_practice_session_started_at (started_at),
    CONSTRAINT fk_kyyy_word_practice_session_user_id FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_kyyy_word_practice_session_word_bank_id FOREIGN KEY (word_bank_id) REFERENCES kyyy_word_bank (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYYY背词会话表';

CREATE TABLE IF NOT EXISTS kyyy_word_practice_session_item (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '背词会话卡片ID',
    session_id BIGINT UNSIGNED NOT NULL COMMENT '背词会话ID',
    word_id BIGINT UNSIGNED NOT NULL COMMENT '单词ID',
    source_type VARCHAR(20) NOT NULL DEFAULT 'new' COMMENT '卡片来源：new/review/relearn',
    round_no INT NOT NULL DEFAULT 1 COMMENT '当前轮次',
    queue_order INT NOT NULL DEFAULT 0 COMMENT '会话内顺序',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '卡片状态：pending/answered',
    rating VARCHAR(20) DEFAULT NULL COMMENT '评分结果：know/fuzzy/unknown',
    scheduled_after_index INT NOT NULL DEFAULT 0 COMMENT '计划插入位置索引',
    shown_at DATETIME DEFAULT NULL COMMENT '展示时间',
    answered_at DATETIME DEFAULT NULL COMMENT '作答时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_kyyy_word_practice_session_item_session_order (session_id, queue_order),
    KEY idx_kyyy_word_practice_session_item_session_status (session_id, status, queue_order),
    KEY idx_kyyy_word_practice_session_item_word_id (word_id),
    CONSTRAINT fk_kyyy_word_practice_session_item_session_id FOREIGN KEY (session_id) REFERENCES kyyy_word_practice_session (id),
    CONSTRAINT fk_kyyy_word_practice_session_item_word_id FOREIGN KEY (word_id) REFERENCES kyyy_word (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYYY背词会话卡片表';

SET FOREIGN_KEY_CHECKS = 1;
