-- ai-index: add kyyy practice setting table
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS kyyy_user_practice_setting (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '刷题设置ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    exam_direction VARCHAR(20) NOT NULL DEFAULT 'english_one' COMMENT '考试方向：english_one英一/english_two英二',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_kyyy_user_practice_setting_user_id (user_id),
    CONSTRAINT fk_kyyy_user_practice_setting_user_id FOREIGN KEY (user_id) REFERENCES app_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYYY用户刷题设置表';
