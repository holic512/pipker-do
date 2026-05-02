-- ai-index: add kyyy word bank core tables
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS kyyy_word_bank (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '词库ID',
    bank_code VARCHAR(50) NOT NULL COMMENT '词库编码',
    bank_name VARCHAR(100) NOT NULL COMMENT '词库名称',
    subtitle VARCHAR(255) DEFAULT NULL COMMENT '词库副标题',
    description TEXT COMMENT '词库描述',
    word_count INT NOT NULL DEFAULT 0 COMMENT '单词数',
    study_user_count INT NOT NULL DEFAULT 0 COMMENT '学习人数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0下架 1上架',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序值',
    created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建后台管理员ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyyy_word_bank_code (bank_code),
    KEY idx_kyyy_word_bank_status_sort_no (status, sort_no),
    KEY idx_kyyy_word_bank_created_by (created_by),
    CONSTRAINT fk_kyyy_word_bank_created_by FOREIGN KEY (created_by) REFERENCES admin_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYYY词库表';

CREATE TABLE IF NOT EXISTS kyyy_word (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '单词ID',
    word_text VARCHAR(100) NOT NULL COMMENT '单词原文',
    normalized_word VARCHAR(100) NOT NULL COMMENT '标准化单词，统一小写去空格',
    phonetic_us VARCHAR(100) DEFAULT NULL COMMENT '美式音标',
    phonetic_uk VARCHAR(100) DEFAULT NULL COMMENT '英式音标',
    part_of_speech VARCHAR(50) DEFAULT NULL COMMENT '词性',
    meaning_cn TEXT COMMENT '中文释义',
    example_sentence VARCHAR(500) DEFAULT NULL COMMENT '例句',
    example_translation VARCHAR(500) DEFAULT NULL COMMENT '例句翻译',
    difficulty_level TINYINT NOT NULL DEFAULT 1 COMMENT '难度等级：1基础 2进阶 3高频 4冲刺',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
    created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建后台管理员ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyyy_word_normalized_word (normalized_word),
    KEY idx_kyyy_word_status_difficulty (status, difficulty_level),
    KEY idx_kyyy_word_created_by (created_by),
    CONSTRAINT fk_kyyy_word_created_by FOREIGN KEY (created_by) REFERENCES admin_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYYY单词表';

CREATE TABLE IF NOT EXISTS kyyy_word_bank_word_rel (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '词库单词关联ID',
    word_bank_id BIGINT UNSIGNED NOT NULL COMMENT '词库ID',
    word_id BIGINT UNSIGNED NOT NULL COMMENT '单词ID',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '词库内排序值',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyyy_word_bank_word_rel_bank_word (word_bank_id, word_id),
    KEY idx_kyyy_word_bank_word_rel_word_id (word_id),
    KEY idx_kyyy_word_bank_word_rel_bank_sort_no (word_bank_id, sort_no),
    CONSTRAINT fk_kyyy_word_bank_word_rel_bank_id FOREIGN KEY (word_bank_id) REFERENCES kyyy_word_bank (id),
    CONSTRAINT fk_kyyy_word_bank_word_rel_word_id FOREIGN KEY (word_id) REFERENCES kyyy_word (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYYY词库单词关联表';

CREATE TABLE IF NOT EXISTS kyyy_user_word_bank (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户词库记录ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    word_bank_id BIGINT UNSIGNED NOT NULL COMMENT '词库ID',
    join_source VARCHAR(30) NOT NULL DEFAULT 'manual' COMMENT '加入来源：manual/recommend/import',
    last_practice_at DATETIME DEFAULT NULL COMMENT '最近背词时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyyy_user_word_bank_user_bank (user_id, word_bank_id),
    KEY idx_kyyy_user_word_bank_word_bank_id (word_bank_id),
    CONSTRAINT fk_kyyy_user_word_bank_user_id FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_kyyy_user_word_bank_word_bank_id FOREIGN KEY (word_bank_id) REFERENCES kyyy_word_bank (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYYY用户词库表';

CREATE TABLE IF NOT EXISTS kyyy_user_word_progress (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户单词进度ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    word_id BIGINT UNSIGNED NOT NULL COMMENT '单词ID',
    study_status VARCHAR(20) NOT NULL DEFAULT 'new' COMMENT '学习状态：new/learning/mastered',
    mastery_level TINYINT NOT NULL DEFAULT 0 COMMENT '掌握等级：0未开始 1陌生 2一般 3掌握 4熟练',
    review_count INT NOT NULL DEFAULT 0 COMMENT '复习次数',
    correct_count INT NOT NULL DEFAULT 0 COMMENT '答对次数',
    wrong_count INT NOT NULL DEFAULT 0 COMMENT '答错次数',
    last_result VARCHAR(20) DEFAULT NULL COMMENT '最近结果：correct/wrong/skip',
    last_studied_at DATETIME DEFAULT NULL COMMENT '最近学习时间',
    next_review_at DATETIME DEFAULT NULL COMMENT '下次复习时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyyy_user_word_progress_user_word (user_id, word_id),
    KEY idx_kyyy_user_word_progress_word_id (word_id),
    KEY idx_kyyy_user_word_progress_user_status_review (user_id, study_status, next_review_at),
    CONSTRAINT fk_kyyy_user_word_progress_user_id FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_kyyy_user_word_progress_word_id FOREIGN KEY (word_id) REFERENCES kyyy_word (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYYY用户单词进度表';

CREATE TABLE IF NOT EXISTS kyyy_word_related (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '单词延伸关系ID',
    word_id BIGINT UNSIGNED NOT NULL COMMENT '主单词ID',
    related_word_id BIGINT UNSIGNED DEFAULT NULL COMMENT '关联单词ID，可为空',
    related_word_text VARCHAR(100) NOT NULL COMMENT '延伸词原文',
    normalized_related_word VARCHAR(100) NOT NULL COMMENT '标准化延伸词，统一小写去空格',
    meaning_cn VARCHAR(255) DEFAULT NULL COMMENT '中文释义',
    relation_type VARCHAR(30) NOT NULL DEFAULT 'derivative' COMMENT '关系类型：derivative/synonym/antonym/phrase/association',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '展示排序值',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyyy_word_related_word_related_word (word_id, normalized_related_word),
    KEY idx_kyyy_word_related_related_word_id (related_word_id),
    KEY idx_kyyy_word_related_word_status_sort (word_id, status, sort_no),
    KEY idx_kyyy_word_related_normalized_word_status (normalized_related_word, status),
    CONSTRAINT fk_kyyy_word_related_word_id FOREIGN KEY (word_id) REFERENCES kyyy_word (id),
    CONSTRAINT fk_kyyy_word_related_related_word_id FOREIGN KEY (related_word_id) REFERENCES kyyy_word (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYYY单词延伸关系表';
