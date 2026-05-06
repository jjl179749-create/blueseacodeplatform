-- ============================================================
-- 管理后台模块 - 增量迁移脚本
-- 适用场景：已在 blueseacode 数据库运行过 init.sql 的情况下，
--           单独为 Phase 8 管理后台建表 + 初始化数据
-- 安全特性：所有 CREATE 使用 IF NOT EXISTS
--           所有 INSERT 使用 INSERT IGNORE，可重复执行
-- ============================================================

USE blueseacode;

-- ========== 系统配置表 ==========
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(200) DEFAULT NULL COMMENT '配置说明',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ========== 公告表 ==========
CREATE TABLE IF NOT EXISTS sys_announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容',
    status TINYINT DEFAULT 0 COMMENT '状态: 0草稿 1发布',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- ========== 轮播图表 ==========
CREATE TABLE IF NOT EXISTS sys_banner (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) DEFAULT NULL COMMENT '标题',
    image_url VARCHAR(500) NOT NULL COMMENT '图片URL',
    link_url VARCHAR(500) DEFAULT NULL COMMENT '跳转链接',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '0隐藏 1显示',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- ========== 操作日志表 ==========
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    username VARCHAR(50) DEFAULT NULL COMMENT '操作人用户名',
    module VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
    action VARCHAR(50) DEFAULT NULL COMMENT '操作类型',
    target_id BIGINT DEFAULT NULL COMMENT '目标ID',
    target_type VARCHAR(50) DEFAULT NULL COMMENT '目标类型',
    request_ip VARCHAR(50) DEFAULT NULL COMMENT '请求IP',
    params TEXT DEFAULT NULL COMMENT '请求参数',
    result VARCHAR(10) DEFAULT NULL COMMENT '操作结果: SUCCESS/FAIL',
    duration BIGINT DEFAULT NULL COMMENT '耗时(毫秒)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_module (module),
    INDEX idx_action (action),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================================
-- 初始化配置数据
-- ============================================================
INSERT IGNORE INTO sys_config (config_key, config_value, description) VALUES
('points.register', '10', '注册赠送积分'),
('points.daily_login', '5', '每日签到积分'),
('points.publish_resource', '20', '发布资源并通过审核奖励积分'),
('points.publish_article', '15', '发布文章并通过审核奖励积分'),
('points.download_resource', '-1', '下载资源消耗积分(按资源设置)'),
('site.name', '蓝海编程平台', '站点名称'),
('site.logo', '/logo.png', '站点Logo'),
('site.seo_keywords', '编程,技术,社区,资源分享', 'SEO关键词'),
('site.icp', '', 'ICP备案号'),
('site.contact_email', 'admin@blueseacode.com', '联系邮箱');

-- ============================================================
-- 初始化权限数据（管理后台菜单树）
-- ============================================================
INSERT IGNORE INTO sys_permission (id, parent_id, name, permission, path, component, type, icon, sort) VALUES
(1, NULL, '仪表盘', 'dashboard', '/dashboard', 'dashboard/index.vue', 1, 'dashboard-icon', 1),
(2, NULL, '用户管理', 'user', '/user', 'user/index.vue', 1, 'user-icon', 2),
(3, NULL, '内容审核', 'audit', '/audit', 'audit/index.vue', 1, 'audit-icon', 3),
(4, NULL, '资源管理', 'resource', '/resource', 'resource/index.vue', 1, 'resource-icon', 4),
(5, NULL, '需求管理', 'demand', '/demand', 'demand/index.vue', 1, 'demand-icon', 5),
(6, NULL, '客服管理', 'cs', '/cs', 'cs/index.vue', 1, 'cs-icon', 6),
(7, NULL, '系统配置', 'system', '/system', 'system/index.vue', 1, 'system-icon', 7),
(8, NULL, '权限管理', 'permission', '/permission', 'permission/index.vue', 1, 'permission-icon', 8),
(9, NULL, '日志管理', 'log', '/log', 'log/index.vue', 1, 'log-icon', 9),
(10, 2, '用户列表', 'user:list', NULL, NULL, 2, NULL, 1),
(11, 2, '禁用用户', 'user:disable', NULL, NULL, 2, NULL, 2),
(12, 2, '分配角色', 'user:assignRole', NULL, NULL, 2, NULL, 3);

-- 用户管理子菜单(id=10,11,12)
