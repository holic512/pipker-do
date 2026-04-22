ALTER TABLE app_user
    ADD COLUMN wechat_openid VARCHAR(64) NULL COMMENT '微信小程序openid' AFTER id,
    ADD COLUMN wechat_unionid VARCHAR(64) NULL COMMENT '微信unionid' AFTER wechat_openid;

ALTER TABLE app_user
    MODIFY COLUMN username VARCHAR(50) NULL COMMENT '用户名',
    MODIFY COLUMN password_hash VARCHAR(255) NULL COMMENT '密码加密',
    MODIFY COLUMN nickname VARCHAR(50) NOT NULL COMMENT '昵称';

ALTER TABLE app_user
    ADD UNIQUE KEY uk_app_user_wechat_openid (wechat_openid),
    ADD KEY idx_app_user_wechat_unionid (wechat_unionid);
