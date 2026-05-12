-- ai-index: backend database baseline schema
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS app_user (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    wechat_openid VARCHAR(64) DEFAULT NULL COMMENT '微信小程序openid',
    wechat_unionid VARCHAR(64) DEFAULT NULL COMMENT '微信unionid',
    username VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    nickname VARCHAR(50) NOT NULL COMMENT '昵称',
    password_hash VARCHAR(255) DEFAULT NULL COMMENT '密码加密',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    avatar_url VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    gender TINYINT NOT NULL DEFAULT 0 COMMENT '性别：0未知 1男 2女',
    bio VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
    agreement_version VARCHAR(30) DEFAULT NULL COMMENT '已接受用户协议版本',
    agreement_accepted_at DATETIME DEFAULT NULL COMMENT '用户协议接受时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
    last_login_at DATETIME DEFAULT NULL COMMENT '最后登录时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_user_username (username),
    UNIQUE KEY uk_app_user_wechat_openid (wechat_openid),
    UNIQUE KEY uk_app_user_phone (phone),
    UNIQUE KEY uk_app_user_email (email),
    KEY idx_app_user_wechat_unionid (wechat_unionid),
    KEY idx_app_user_agreement_version (agreement_version, agreement_accepted_at),
    KEY idx_app_user_status_created_at (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS app_user_project_preference (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户项目偏好ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    default_project_code VARCHAR(50) NOT NULL COMMENT '默认项目编码',
    last_visit_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近项目访问时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_user_project_preference_user_id (user_id),
    KEY idx_app_user_project_preference_project_visit (default_project_code, last_visit_at),
    CONSTRAINT fk_app_user_project_preference_user_id FOREIGN KEY (user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户默认项目偏好表';

CREATE TABLE IF NOT EXISTS app_user_vip (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'VIP记录ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    vip_type VARCHAR(30) NOT NULL COMMENT 'VIP类型：month/quarter/year/lifetime/custom',
    vip_status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0已失效 1生效中 2已退款',
    source_type VARCHAR(30) NOT NULL DEFAULT 'manual' COMMENT '来源：manual/order/activity/card',
    source_ref_id BIGINT UNSIGNED DEFAULT NULL COMMENT '来源关联ID，例如兑换Key ID',
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '支付金额',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    invalid_reason VARCHAR(255) DEFAULT NULL COMMENT '作废原因',
    invalid_at DATETIME DEFAULT NULL COMMENT '作废时间',
    invalid_by BIGINT UNSIGNED DEFAULT NULL COMMENT '作废后台管理员ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_app_user_vip_user_status_end_time (user_id, vip_status, end_time),
    KEY idx_app_user_vip_status_end_time (vip_status, end_time),
    KEY idx_app_user_vip_source (source_type, source_ref_id),
    KEY idx_app_user_vip_invalid_by (invalid_by),
    CONSTRAINT fk_app_user_vip_user_id FOREIGN KEY (user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户VIP表';

CREATE TABLE IF NOT EXISTS vip_card_group (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'VIP卡组ID',
    group_name VARCHAR(80) NOT NULL COMMENT '卡组名称',
    vip_type VARCHAR(30) NOT NULL COMMENT 'VIP类型：month/quarter/year/lifetime/custom',
    duration_days INT NOT NULL DEFAULT 30 COMMENT '会员有效天数，永久会员使用0',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建后台管理员ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_vip_card_group_status_created_at (status, created_at),
    KEY idx_vip_card_group_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='VIP兑换卡组表';

CREATE TABLE IF NOT EXISTS vip_card_key (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'VIP兑换Key ID',
    group_id BIGINT UNSIGNED NOT NULL COMMENT '所属卡组ID',
    card_key VARCHAR(64) NOT NULL COMMENT '兑换Key明文',
    batch_no VARCHAR(40) DEFAULT NULL COMMENT '生成批次号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0已作废 1未使用 2已兑换',
    redeemed_user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '兑换用户ID',
    redeemed_at DATETIME DEFAULT NULL COMMENT '兑换时间',
    voided_at DATETIME DEFAULT NULL COMMENT '作废时间',
    void_reason VARCHAR(255) DEFAULT NULL COMMENT '作废原因',
    created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建后台管理员ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_vip_card_key_card_key (card_key),
    KEY idx_vip_card_key_group_status (group_id, status),
    KEY idx_vip_card_key_group_batch (group_id, batch_no),
    KEY idx_vip_card_key_status_created_at (status, created_at),
    KEY idx_vip_card_key_redeemed_user (redeemed_user_id, redeemed_at),
    KEY idx_vip_card_key_created_by (created_by),
    CONSTRAINT fk_vip_card_key_group_id FOREIGN KEY (group_id) REFERENCES vip_card_group (id),
    CONSTRAINT fk_vip_card_key_redeemed_user_id FOREIGN KEY (redeemed_user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='VIP兑换Key表';

CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '后台管理员ID',
    username VARCHAR(50) NOT NULL COMMENT '登录账号',
    display_name VARCHAR(50) NOT NULL COMMENT '显示名称',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
    default_project_code VARCHAR(50) DEFAULT NULL COMMENT '默认项目编码',
    last_login_at DATETIME DEFAULT NULL COMMENT '最近登录时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_user_username (username),
    KEY idx_admin_user_status_created_at (status, created_at),
    KEY idx_admin_user_default_project_code (default_project_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台管理员表';

CREATE TABLE IF NOT EXISTS admin_role (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '后台角色ID',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_role_code (role_code),
    KEY idx_admin_role_status_created_at (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台角色表';

CREATE TABLE IF NOT EXISTS admin_user_role (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '管理员角色关系ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '管理员ID',
    role_id BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_user_role_user_role (user_id, role_id),
    KEY idx_admin_user_role_role_id (role_id),
    CONSTRAINT fk_admin_user_role_user_id FOREIGN KEY (user_id) REFERENCES admin_user (id),
    CONSTRAINT fk_admin_user_role_role_id FOREIGN KEY (role_id) REFERENCES admin_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台管理员角色关系表';

CREATE TABLE IF NOT EXISTS admin_project_access (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '管理员项目授权ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '管理员ID',
    project_code VARCHAR(50) NOT NULL COMMENT '项目编码',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0否 1是',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_project_access_user_project (user_id, project_code),
    KEY idx_admin_project_access_project_enabled (project_code, enabled),
    CONSTRAINT fk_admin_project_access_user_id FOREIGN KEY (user_id) REFERENCES admin_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台管理员项目授权表';

CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '系统配置ID',
    config_group VARCHAR(50) NOT NULL COMMENT '配置分组',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_name VARCHAR(100) NOT NULL COMMENT '配置名称',
    config_type VARCHAR(20) NOT NULL DEFAULT 'string' COMMENT '配置类型：string/number/boolean/secret/json',
    config_value TEXT COMMENT '配置值，敏感配置为密文',
    is_sensitive TINYINT NOT NULL DEFAULT 0 COMMENT '是否敏感：0否 1是',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0否 1是',
    description VARCHAR(255) DEFAULT NULL COMMENT '配置说明',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序值',
    updated_by BIGINT UNSIGNED DEFAULT NULL COMMENT '最后更新管理员ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_config_key (config_key),
    KEY idx_sys_config_group_sort (config_group, sort_no),
    KEY idx_sys_config_updated_by (updated_by),
    CONSTRAINT fk_sys_config_updated_by FOREIGN KEY (updated_by) REFERENCES admin_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

CREATE TABLE IF NOT EXISTS sys_config_change_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '配置变更日志ID',
    config_group VARCHAR(50) NOT NULL COMMENT '配置分组',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    old_value_masked TEXT COMMENT '旧值脱敏快照',
    new_value_masked TEXT COMMENT '新值脱敏快照',
    changed_by BIGINT UNSIGNED DEFAULT NULL COMMENT '操作管理员ID',
    request_id VARCHAR(64) DEFAULT NULL COMMENT '请求ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_sys_config_change_log_key_created_at (config_key, created_at),
    KEY idx_sys_config_change_log_group_created_at (config_group, created_at),
    KEY idx_sys_config_change_log_changed_by (changed_by),
    CONSTRAINT fk_sys_config_change_log_changed_by FOREIGN KEY (changed_by) REFERENCES admin_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置变更日志表';

CREATE TABLE IF NOT EXISTS llm_call_record (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'LLM调用记录ID',
    scene VARCHAR(80) NOT NULL COMMENT '调用场景',
    model VARCHAR(80) DEFAULT NULL COMMENT '模型',
    status VARCHAR(20) NOT NULL COMMENT '状态：success/failed',
    latency_ms BIGINT NOT NULL DEFAULT 0 COMMENT '耗时毫秒',
    input_tokens INT DEFAULT NULL COMMENT '输入token数',
    output_tokens INT DEFAULT NULL COMMENT '输出token数',
    total_tokens INT DEFAULT NULL COMMENT '总token数',
    request_id VARCHAR(64) DEFAULT NULL COMMENT '请求ID',
    prompt_hash VARCHAR(64) DEFAULT NULL COMMENT 'prompt摘要哈希',
    input_preview TEXT COMMENT '输入截断预览',
    output_preview TEXT COMMENT '输出截断预览',
    error_message VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    operator_id BIGINT UNSIGNED DEFAULT NULL COMMENT '触发后台管理员ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_llm_call_record_status_created_at (status, created_at),
    KEY idx_llm_call_record_scene_created_at (scene, created_at),
    KEY idx_llm_call_record_request_id (request_id),
    KEY idx_llm_call_record_operator_id (operator_id),
    CONSTRAINT fk_llm_call_record_operator_id FOREIGN KEY (operator_id) REFERENCES admin_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM调用记录表';


CREATE TABLE IF NOT EXISTS kyzz_category (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    category_code VARCHAR(50) NOT NULL COMMENT '分类编码',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    category_level TINYINT NOT NULL DEFAULT 1 COMMENT '层级',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序值',
    is_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0否 1是',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyzz_category_code (category_code),
    KEY idx_kyzz_category_enabled_sort_no (is_enabled, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考研政治分类表';

CREATE TABLE IF NOT EXISTS kyzz_question_bank (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '题库ID',
    bank_code VARCHAR(50) NOT NULL COMMENT '题库编码',
    bank_name VARCHAR(100) NOT NULL COMMENT '题库名称',
    subtitle VARCHAR(255) DEFAULT NULL COMMENT '题库副标题',
    cover_url VARCHAR(255) DEFAULT NULL COMMENT '封面图',
    description TEXT COMMENT '题库介绍',
    category_id BIGINT UNSIGNED DEFAULT NULL COMMENT '主分类ID',
    difficulty_level TINYINT NOT NULL DEFAULT 2 COMMENT '难度：1简单 2中等 3困难 4冲刺',
    question_count INT NOT NULL DEFAULT 0 COMMENT '题目数',
    total_score DECIMAL(4,2) NOT NULL DEFAULT 0.00 COMMENT '综合评分',
    rating_count INT NOT NULL DEFAULT 0 COMMENT '评分人数',
    collect_count INT NOT NULL DEFAULT 0 COMMENT '收藏人数',
    study_user_count INT NOT NULL DEFAULT 0 COMMENT '学习人数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0下架 1上架',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序值',
    created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建后台管理员ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyzz_question_bank_code (bank_code),
    KEY idx_kyzz_question_bank_category_status_sort_no (category_id, status, sort_no),
    KEY idx_kyzz_question_bank_status_sort_no (status, sort_no),
    KEY idx_kyzz_question_bank_created_by (created_by),
    CONSTRAINT fk_kyzz_question_bank_category_id FOREIGN KEY (category_id) REFERENCES kyzz_category (id),
    CONSTRAINT fk_kyzz_question_bank_created_by FOREIGN KEY (created_by) REFERENCES admin_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题库表';

CREATE TABLE IF NOT EXISTS kyzz_question (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '题目ID',
    question_bank_id BIGINT UNSIGNED NOT NULL COMMENT '所属题库ID',
    category_id BIGINT UNSIGNED DEFAULT NULL COMMENT '分类ID',
    question_type VARCHAR(20) NOT NULL COMMENT '题型：single/multiple/short',
    stem TEXT NOT NULL COMMENT '题干',
    analysis TEXT COMMENT '解析',
    answer_text TEXT COMMENT '标准答案，主观题可直接存文本',
    difficulty_level TINYINT NOT NULL DEFAULT 2 COMMENT '难度：1简单 2中等 3困难',
    score DECIMAL(6,2) NOT NULL DEFAULT 1.00 COMMENT '分值',
    source_name VARCHAR(100) DEFAULT NULL COMMENT '题目来源',
    year_no SMALLINT DEFAULT NULL COMMENT '年份',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序值',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_kyzz_question_bank_status_sort_no (question_bank_id, status, sort_no),
    KEY idx_kyzz_question_category_status_sort_no (category_id, status, sort_no),
    KEY idx_kyzz_question_type_status (question_type, status),
    CONSTRAINT fk_kyzz_question_bank_id FOREIGN KEY (question_bank_id) REFERENCES kyzz_question_bank (id),
    CONSTRAINT fk_kyzz_question_category_id FOREIGN KEY (category_id) REFERENCES kyzz_category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目表';

CREATE TABLE IF NOT EXISTS kyzz_question_option (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '选项ID',
    question_id BIGINT UNSIGNED NOT NULL COMMENT '题目ID',
    option_key VARCHAR(10) NOT NULL COMMENT '选项标识：A/B/C/D',
    option_content TEXT NOT NULL COMMENT '选项内容',
    is_correct TINYINT NOT NULL DEFAULT 0 COMMENT '是否正确：0否 1是',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序值',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyzz_question_option_question_id_option_key (question_id, option_key),
    CONSTRAINT fk_kyzz_question_option_question_id FOREIGN KEY (question_id) REFERENCES kyzz_question (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目选项表';

CREATE TABLE IF NOT EXISTS kyzz_tag (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    tag_type VARCHAR(30) NOT NULL DEFAULT 'question' COMMENT '标签类型：question/bank',
    color VARCHAR(20) DEFAULT NULL COMMENT '标签颜色',
    use_count INT NOT NULL DEFAULT 0 COMMENT '使用次数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyzz_tag_name_type (tag_name, tag_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

CREATE TABLE IF NOT EXISTS kyzz_question_tag_rel (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    question_id BIGINT UNSIGNED NOT NULL COMMENT '题目ID',
    tag_id BIGINT UNSIGNED NOT NULL COMMENT '标签ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyzz_question_tag_rel_question_id_tag_id (question_id, tag_id),
    KEY idx_kyzz_question_tag_rel_tag_id (tag_id),
    CONSTRAINT fk_kyzz_question_tag_rel_question_id FOREIGN KEY (question_id) REFERENCES kyzz_question (id),
    CONSTRAINT fk_kyzz_question_tag_rel_tag_id FOREIGN KEY (tag_id) REFERENCES kyzz_tag (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目标签关联表';

CREATE TABLE IF NOT EXISTS kyzz_question_bank_rating (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评分ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    question_bank_id BIGINT UNSIGNED NOT NULL COMMENT '题库ID',
    score TINYINT NOT NULL COMMENT '评分：1-5',
    content VARCHAR(255) DEFAULT NULL COMMENT '评分简评',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyzz_question_bank_rating_user_bank (user_id, question_bank_id),
    KEY idx_kyzz_question_bank_rating_bank_id (question_bank_id),
    CONSTRAINT fk_kyzz_question_bank_rating_user_id FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_kyzz_question_bank_rating_bank_id FOREIGN KEY (question_bank_id) REFERENCES kyzz_question_bank (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题库评分表';

CREATE TABLE IF NOT EXISTS kyzz_user_question_bank (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户题库记录ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    question_bank_id BIGINT UNSIGNED NOT NULL COMMENT '题库ID',
    join_source VARCHAR(30) NOT NULL DEFAULT 'manual' COMMENT '加入来源：manual/recommend/vip',
    current_progress DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '当前进度百分比',
    studied_count INT NOT NULL DEFAULT 0 COMMENT '已学习题数',
    correct_count INT NOT NULL DEFAULT 0 COMMENT '正确题数',
    wrong_count INT NOT NULL DEFAULT 0 COMMENT '错误题数',
    last_practice_at DATETIME DEFAULT NULL COMMENT '最后练习时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyzz_user_question_bank_user_bank (user_id, question_bank_id),
    KEY idx_kyzz_user_question_bank_bank_id (question_bank_id),
    CONSTRAINT fk_kyzz_user_question_bank_user_id FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_kyzz_user_question_bank_bank_id FOREIGN KEY (question_bank_id) REFERENCES kyzz_question_bank (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户题库表';

CREATE TABLE IF NOT EXISTS kyzz_user_practice_setting (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '刷题设置ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    auto_jump_on_correct TINYINT NOT NULL DEFAULT 1 COMMENT '答案正确后是否自动进入下一题：0否 1是',
    bank_practice_choice_only TINYINT NOT NULL DEFAULT 0 COMMENT '刷自选题库时是否只刷选择题：0否 1是',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyzz_user_practice_setting_user_id (user_id),
    CONSTRAINT fk_kyzz_user_practice_setting_user_id FOREIGN KEY (user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYZZ用户刷题设置表';

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

CREATE TABLE IF NOT EXISTS kyyy_user_practice_setting (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '刷题设置ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    exam_direction VARCHAR(20) NOT NULL DEFAULT 'english_one' COMMENT '考试方向：english_one英一/english_two英二',
    default_word_bank_id BIGINT UNSIGNED DEFAULT NULL COMMENT '默认词库ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyyy_user_practice_setting_user_id (user_id),
    KEY idx_kyyy_user_practice_setting_default_word_bank_id (default_word_bank_id),
    CONSTRAINT fk_kyyy_user_practice_setting_user_id FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_kyyy_user_practice_setting_default_word_bank_id FOREIGN KEY (default_word_bank_id) REFERENCES kyyy_word_bank (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYYY用户刷题设置表';

CREATE TABLE IF NOT EXISTS kyyy_word (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '单词ID',
    word_text VARCHAR(100) NOT NULL COMMENT '单词原文',
    normalized_word VARCHAR(100) NOT NULL COMMENT '标准化单词，统一小写去空格',
    phonetic_us VARCHAR(100) DEFAULT NULL COMMENT '美式音标',
    phonetic_uk VARCHAR(100) DEFAULT NULL COMMENT '英式音标',
    part_of_speech VARCHAR(50) DEFAULT NULL COMMENT '词性',
    meaning_cn TEXT COMMENT '中文释义',
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
    study_status VARCHAR(20) NOT NULL DEFAULT 'new' COMMENT '学习状态：new/learning/review/relearning/mastered',
    mastery_level TINYINT NOT NULL DEFAULT 0 COMMENT '掌握等级：0未开始 1陌生 2一般 3掌握 4熟练',
    memory_stage TINYINT NOT NULL DEFAULT 0 COMMENT '长期记忆阶段：0-5',
    learning_step TINYINT NOT NULL DEFAULT 0 COMMENT '短期学习步进：0未开始 1首轮通过 2已毕业',
    lapse_count INT NOT NULL DEFAULT 0 COMMENT '遗忘次数',
    consecutive_known_count INT NOT NULL DEFAULT 0 COMMENT '连续认识次数',
    review_count INT NOT NULL DEFAULT 0 COMMENT '复习次数',
    correct_count INT NOT NULL DEFAULT 0 COMMENT '答对次数',
    wrong_count INT NOT NULL DEFAULT 0 COMMENT '答错次数',
    last_result VARCHAR(20) DEFAULT NULL COMMENT '最近结果：know/fuzzy/unknown',
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

CREATE TABLE IF NOT EXISTS kyyy_word_related (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '单词延伸关系ID',
    word_id BIGINT UNSIGNED NOT NULL COMMENT '主单词ID',
    related_word_id BIGINT UNSIGNED DEFAULT NULL COMMENT '关联单词ID，可为空',
    related_word_text VARCHAR(100) NOT NULL COMMENT '延伸词原文',
    normalized_related_word VARCHAR(100) NOT NULL COMMENT '标准化延伸词，统一小写去空格',
    meaning_cn VARCHAR(255) DEFAULT NULL COMMENT '中文释义',
    relation_type VARCHAR(30) NOT NULL DEFAULT 'derivative' COMMENT '关系类型：derivative/synonym/antonym/phrase/association',
    source_type VARCHAR(30) NOT NULL DEFAULT 'manual' COMMENT '来源类型：manual/ai/import',
    source_run_id VARCHAR(64) DEFAULT NULL COMMENT 'AI补齐运行ID',
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

CREATE TABLE IF NOT EXISTS kyyy_word_example (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '单词例句ID',
    word_id BIGINT UNSIGNED NOT NULL COMMENT '单词ID',
    example_sentence VARCHAR(500) NOT NULL COMMENT '英文例句',
    example_translation VARCHAR(500) NOT NULL COMMENT '例句中文翻译',
    example_hash CHAR(64) NOT NULL COMMENT '例句标准化SHA-256',
    source_type VARCHAR(30) NOT NULL DEFAULT 'manual' COMMENT '来源类型：manual/ai/import/legacy',
    source_run_id VARCHAR(64) DEFAULT NULL COMMENT 'AI补齐运行ID',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '展示排序值',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyyy_word_example_word_hash (word_id, example_hash),
    KEY idx_kyyy_word_example_word_status_sort (word_id, status, sort_no),
    KEY idx_kyyy_word_example_source_run_id (source_run_id),
    CONSTRAINT fk_kyyy_word_example_word_id FOREIGN KEY (word_id) REFERENCES kyyy_word (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYYY单词多例句表';

CREATE TABLE IF NOT EXISTS kyyy_word_ai_enrichment_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '词库AI补齐日志ID',
    run_id VARCHAR(64) NOT NULL COMMENT '运行ID',
    word_id BIGINT UNSIGNED DEFAULT NULL COMMENT '单词ID',
    word_text VARCHAR(100) DEFAULT NULL COMMENT '单词原文快照',
    run_mode VARCHAR(20) NOT NULL COMMENT '运行模式：single/all/validate',
    status VARCHAR(30) NOT NULL COMMENT '状态：skipped/success/failed/validation_failed',
    needs_ai TINYINT NOT NULL DEFAULT 0 COMMENT '是否判定需要AI补齐：0否 1是',
    reason VARCHAR(500) DEFAULT NULL COMMENT '校验或跳过原因',
    model VARCHAR(80) DEFAULT NULL COMMENT '调用模型',
    prompt_hash CHAR(64) DEFAULT NULL COMMENT 'Prompt SHA-256',
    response_hash CHAR(64) DEFAULT NULL COMMENT '响应 SHA-256',
    example_count INT NOT NULL DEFAULT 0 COMMENT '写入例句数',
    synonym_count INT NOT NULL DEFAULT 0 COMMENT '写入近义词数',
    input_tokens INT DEFAULT NULL COMMENT '输入token数',
    output_tokens INT DEFAULT NULL COMMENT '输出token数',
    total_tokens INT DEFAULT NULL COMMENT '总token数',
    error_message VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_kyyy_word_ai_enrichment_log_run_id (run_id),
    KEY idx_kyyy_word_ai_enrichment_log_word_status (word_id, status),
    CONSTRAINT fk_kyyy_word_ai_enrichment_log_word_id FOREIGN KEY (word_id) REFERENCES kyyy_word (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYYY词库AI补齐日志表';

CREATE TABLE IF NOT EXISTS kyzz_exam_session (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '考试会话ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    exam_no VARCHAR(40) NOT NULL COMMENT '考试编号',
    exam_type VARCHAR(20) NOT NULL COMMENT '考试类型：choice/short/full',
    difficulty_mode VARCHAR(20) NOT NULL DEFAULT 'balanced' COMMENT '难度模式：balanced/easy/normal/hard',
    duration_minutes INT NOT NULL DEFAULT 180 COMMENT '考试时长分钟',
    single_count INT NOT NULL DEFAULT 0 COMMENT '单选题数',
    multiple_count INT NOT NULL DEFAULT 0 COMMENT '多选题数',
    short_count INT NOT NULL DEFAULT 0 COMMENT '简答题数',
    total_question_count INT NOT NULL DEFAULT 0 COMMENT '总题数',
    answered_count INT NOT NULL DEFAULT 0 COMMENT '已答题数',
    total_score DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '总分',
    earned_score DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '实得总分',
    objective_score DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '客观题得分',
    subjective_score DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '主观题得分',
    status VARCHAR(20) NOT NULL DEFAULT 'in_progress' COMMENT '状态：in_progress/submitted/expired',
    grading_status VARCHAR(20) NOT NULL DEFAULT 'not_started' COMMENT '阅卷状态：not_started/grading/graded/failed',
    started_at DATETIME NOT NULL COMMENT '开始时间',
    deadline_at DATETIME NOT NULL COMMENT '截止时间',
    submitted_at DATETIME DEFAULT NULL COMMENT '交卷时间',
    grading_started_at DATETIME DEFAULT NULL COMMENT '阅卷开始时间',
    graded_at DATETIME DEFAULT NULL COMMENT '阅卷完成时间',
    grading_error_message VARCHAR(500) DEFAULT NULL COMMENT '阅卷失败原因',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyzz_exam_session_exam_no (exam_no),
    KEY idx_kyzz_exam_session_user_status_deadline (user_id, status, deadline_at),
    KEY idx_kyzz_exam_session_user_started_at (user_id, started_at),
    KEY idx_kyzz_exam_session_grading_status (grading_status, graded_at),
    CONSTRAINT fk_kyzz_exam_session_user_id FOREIGN KEY (user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYZZ VIP考试会话表';

CREATE TABLE IF NOT EXISTS kyzz_exam_question (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '考试题目ID',
    session_id BIGINT UNSIGNED NOT NULL COMMENT '考试会话ID',
    question_id BIGINT UNSIGNED NOT NULL COMMENT '题目ID',
    question_bank_id BIGINT UNSIGNED NOT NULL COMMENT '题库ID',
    question_type VARCHAR(20) NOT NULL COMMENT '题型：single/multiple/short',
    question_order INT NOT NULL DEFAULT 0 COMMENT '试卷内排序',
    score DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '本题分值',
    answer_content TEXT COMMENT '用户暂存答案',
    answer_status TINYINT NOT NULL DEFAULT 0 COMMENT '答题状态：0未答 1已答',
    awarded_score DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '本题得分',
    is_correct TINYINT DEFAULT NULL COMMENT '是否正确：0否 1是，主观题可为空',
    grading_status VARCHAR(20) NOT NULL DEFAULT 'not_started' COMMENT '阅卷状态：not_started/grading/graded/failed',
    grading_method VARCHAR(20) DEFAULT NULL COMMENT '阅卷方式：objective/llm/blank',
    reference_answer TEXT COMMENT '阅卷时参考答案快照',
    analysis_snapshot TEXT COMMENT '阅卷时解析快照',
    grading_comment TEXT COMMENT 'AI阅卷评语',
    grading_confidence DECIMAL(5,4) DEFAULT NULL COMMENT 'AI阅卷置信度',
    grading_points_json TEXT COMMENT 'AI评分点JSON',
    llm_record_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'LLM调用记录ID',
    used_seconds INT NOT NULL DEFAULT 0 COMMENT '本题累计耗时秒',
    answered_at DATETIME DEFAULT NULL COMMENT '最后作答时间',
    graded_at DATETIME DEFAULT NULL COMMENT '阅卷完成时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyzz_exam_question_session_question (session_id, question_id),
    KEY idx_kyzz_exam_question_session_order (session_id, question_order),
    KEY idx_kyzz_exam_question_question_id (question_id),
    KEY idx_kyzz_exam_question_bank_id (question_bank_id),
    KEY idx_kyzz_exam_question_grading_status (grading_status, graded_at),
    KEY idx_kyzz_exam_question_llm_record_id (llm_record_id),
    CONSTRAINT fk_kyzz_exam_question_session_id FOREIGN KEY (session_id) REFERENCES kyzz_exam_session (id),
    CONSTRAINT fk_kyzz_exam_question_question_id FOREIGN KEY (question_id) REFERENCES kyzz_question (id),
    CONSTRAINT fk_kyzz_exam_question_bank_id FOREIGN KEY (question_bank_id) REFERENCES kyzz_question_bank (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYZZ VIP考试题目快照表';

CREATE TABLE IF NOT EXISTS kyzz_user_answer (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户答题记录ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    question_id BIGINT UNSIGNED NOT NULL COMMENT '题目ID',
    question_bank_id BIGINT UNSIGNED NOT NULL COMMENT '题库ID',
    practice_source_type VARCHAR(30) NOT NULL DEFAULT 'bank' COMMENT '练习来源：bank/wrong_book/favorite',
    answer_content TEXT COMMENT '用户答案，选择题可存JSON数组或逗号分隔值',
    is_correct TINYINT NOT NULL DEFAULT 0 COMMENT '是否正确：0否 1是',
    answer_status TINYINT NOT NULL DEFAULT 1 COMMENT '答题状态：1已作答 2已跳过',
    used_seconds INT NOT NULL DEFAULT 0 COMMENT '耗时秒数',
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_kyzz_user_answer_user_bank_submitted_at (user_id, question_bank_id, submitted_at),
    KEY idx_kyzz_user_answer_user_question_submitted_at (user_id, question_id, submitted_at),
    KEY idx_kyzz_user_answer_user_source_bank_submitted_at (user_id, practice_source_type, question_bank_id, submitted_at),
    KEY idx_kyzz_user_answer_user_source_question_submitted_at (user_id, practice_source_type, question_id, submitted_at),
    KEY idx_kyzz_user_answer_question_id (question_id),
    KEY idx_kyzz_user_answer_bank_id (question_bank_id),
    KEY idx_kyzz_user_answer_submitted_at (submitted_at),
    CONSTRAINT fk_kyzz_user_answer_user_id FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_kyzz_user_answer_question_id FOREIGN KEY (question_id) REFERENCES kyzz_question (id),
    CONSTRAINT fk_kyzz_user_answer_bank_id FOREIGN KEY (question_bank_id) REFERENCES kyzz_question_bank (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户答题记录表';

CREATE TABLE IF NOT EXISTS kyzz_user_favorite (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    target_id BIGINT UNSIGNED NOT NULL COMMENT '目标ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyzz_user_favorite_user_target (user_id, target_id),
    KEY idx_kyzz_user_favorite_user_id (user_id),
    KEY idx_kyzz_user_favorite_target_id (target_id),
    CONSTRAINT fk_kyzz_user_favorite_user_id FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_kyzz_user_favorite_target_id FOREIGN KEY (target_id) REFERENCES kyzz_question (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

CREATE TABLE IF NOT EXISTS kyzz_user_wrong_question (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '错题ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    question_id BIGINT UNSIGNED NOT NULL COMMENT '题目ID',
    question_bank_id BIGINT UNSIGNED NOT NULL COMMENT '题库ID',
    first_wrong_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次错题时间',
    last_wrong_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近错题时间',
    wrong_count INT NOT NULL DEFAULT 1 COMMENT '累计错题次数',
    is_mastered TINYINT NOT NULL DEFAULT 0 COMMENT '是否掌握：0否 1是',
    mastered_at DATETIME DEFAULT NULL COMMENT '掌握时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyzz_user_wrong_question_user_question (user_id, question_id),
    KEY idx_kyzz_user_wrong_question_user_mastered_last_wrong_at (user_id, is_mastered, last_wrong_at),
    KEY idx_kyzz_user_wrong_question_bank_mastered (question_bank_id, is_mastered),
    CONSTRAINT fk_kyzz_user_wrong_question_user_id FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_kyzz_user_wrong_question_question_id FOREIGN KEY (question_id) REFERENCES kyzz_question (id),
    CONSTRAINT fk_kyzz_user_wrong_question_bank_id FOREIGN KEY (question_bank_id) REFERENCES kyzz_question_bank (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户错题表';

CREATE TABLE IF NOT EXISTS kyzz_comment (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '评论用户ID',
    target_type VARCHAR(20) NOT NULL COMMENT '评论目标：question/bank',
    target_id BIGINT UNSIGNED NOT NULL COMMENT '目标ID',
    parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父评论ID，0表示一级评论',
    reply_to_user_id BIGINT UNSIGNED DEFAULT NULL COMMENT '回复目标用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    reply_count INT NOT NULL DEFAULT 0 COMMENT '回复数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0删除 1正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_kyzz_comment_target_parent_status_created_at (target_type, target_id, parent_id, status, created_at),
    KEY idx_kyzz_comment_parent_id_created_at (parent_id, created_at),
    KEY idx_kyzz_comment_user_id_created_at (user_id, created_at),
    KEY idx_kyzz_comment_reply_to_user_id (reply_to_user_id),
    CONSTRAINT fk_kyzz_comment_user_id FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_kyzz_comment_reply_to_user_id FOREIGN KEY (reply_to_user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表，支持回复';

CREATE TABLE IF NOT EXISTS kyzz_comment_like (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论点赞ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    comment_id BIGINT UNSIGNED NOT NULL COMMENT '评论ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyzz_comment_like_user_comment (user_id, comment_id),
    KEY idx_kyzz_comment_like_comment_id (comment_id),
    CONSTRAINT fk_kyzz_comment_like_user_id FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_kyzz_comment_like_comment_id FOREIGN KEY (comment_id) REFERENCES kyzz_comment (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞表';

CREATE TABLE IF NOT EXISTS kyzz_leaderboard (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '排行榜记录ID',
    leaderboard_type VARCHAR(20) NOT NULL COMMENT '榜单类型：daily/weekly/monthly/total',
    stat_date DATE NOT NULL COMMENT '统计日期',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    study_count INT NOT NULL DEFAULT 0 COMMENT '学习题数',
    correct_count INT NOT NULL DEFAULT 0 COMMENT '正确题数',
    accuracy_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '正确率百分比',
    score_value DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '综合积分',
    rank_no INT NOT NULL COMMENT '排名',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyzz_leaderboard_type_date_user (leaderboard_type, stat_date, user_id),
    KEY idx_kyzz_leaderboard_type_date_rank (leaderboard_type, stat_date, rank_no),
    CONSTRAINT fk_kyzz_leaderboard_user_id FOREIGN KEY (user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排行榜快照表';

SET FOREIGN_KEY_CHECKS = 1;
