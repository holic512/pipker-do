CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    username VARCHAR(50) NOT NULL COMMENT '登录账号',
    display_name VARCHAR(50) NOT NULL COMMENT '显示名称',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码加密',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
    default_project_code VARCHAR(50) DEFAULT NULL COMMENT '默认项目编码',
    last_login_at DATETIME DEFAULT NULL COMMENT '最后登录时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台管理员表';

CREATE TABLE IF NOT EXISTS admin_role (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台角色表';

CREATE TABLE IF NOT EXISTS admin_user_role (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '管理员ID',
    role_id BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_user_role_user_role (user_id, role_id),
    CONSTRAINT fk_admin_user_role_user_id FOREIGN KEY (user_id) REFERENCES admin_user (id),
    CONSTRAINT fk_admin_user_role_role_id FOREIGN KEY (role_id) REFERENCES admin_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台管理员角色关联表';

CREATE TABLE IF NOT EXISTS admin_project_access (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '项目权限ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '管理员ID',
    project_code VARCHAR(50) NOT NULL COMMENT '项目编码',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_project_access_user_project (user_id, project_code),
    KEY idx_admin_project_access_project_code (project_code),
    CONSTRAINT fk_admin_project_access_user_id FOREIGN KEY (user_id) REFERENCES admin_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台项目访问权限表';

INSERT INTO admin_role (role_code, role_name, status)
SELECT 'SUPER_ADMIN', '超级管理员', 1
WHERE NOT EXISTS (
    SELECT 1 FROM admin_role WHERE role_code = 'SUPER_ADMIN'
);

INSERT INTO admin_user (username, display_name, password_hash, status, default_project_code)
SELECT 'admin', '系统管理员', 'kd2ADLH1illislyKYg3eiQ==:6hxobqm7Wkkr1MhDM8g5tHYtfsdQMj7dwtgVAhqMMJQ=', 1, 'kyzz'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_user WHERE username = 'admin'
);

INSERT INTO admin_user_role (user_id, role_id)
SELECT u.id, r.id
FROM admin_user u
JOIN admin_role r ON r.role_code = 'SUPER_ADMIN'
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM admin_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

INSERT INTO admin_project_access (user_id, project_code, enabled)
SELECT u.id, 'kyzz', 1
FROM admin_user u
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM admin_project_access pa WHERE pa.user_id = u.id AND pa.project_code = 'kyzz'
  );

INSERT INTO admin_project_access (user_id, project_code, enabled)
SELECT u.id, 'kysx', 1
FROM admin_user u
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM admin_project_access pa WHERE pa.user_id = u.id AND pa.project_code = 'kysx'
  );
