SET NAMES utf8mb4;

ALTER TABLE vip_card_key
    ADD COLUMN batch_no VARCHAR(40) DEFAULT NULL COMMENT '生成批次号' AFTER card_key,
    ADD KEY idx_vip_card_key_group_batch (group_id, batch_no);
