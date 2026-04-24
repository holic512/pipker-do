SET NAMES utf8mb4;

ALTER TABLE app_user_vip
    ADD COLUMN source_ref_id BIGINT UNSIGNED DEFAULT NULL COMMENT '来源关联ID，例如兑换Key ID' AFTER source_type,
    ADD COLUMN invalid_reason VARCHAR(255) DEFAULT NULL COMMENT '作废原因' AFTER end_time,
    ADD COLUMN invalid_at DATETIME DEFAULT NULL COMMENT '作废时间' AFTER invalid_reason,
    ADD COLUMN invalid_by BIGINT UNSIGNED DEFAULT NULL COMMENT '作废后台管理员ID' AFTER invalid_at,
    ADD KEY idx_app_user_vip_source (source_type, source_ref_id),
    ADD KEY idx_app_user_vip_invalid_by (invalid_by);

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
    KEY idx_vip_card_group_created_by (created_by),
    CONSTRAINT fk_vip_card_group_created_by FOREIGN KEY (created_by) REFERENCES admin_user (id)
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
    CONSTRAINT fk_vip_card_key_redeemed_user_id FOREIGN KEY (redeemed_user_id) REFERENCES app_user (id),
    CONSTRAINT fk_vip_card_key_created_by FOREIGN KEY (created_by) REFERENCES admin_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='VIP兑换Key表';
