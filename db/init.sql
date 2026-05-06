-- ============================================================
-- 蓝海编程平台 (BlueSeaCode) - 数据库初始化脚本
-- ============================================================
-- 使用方式：
--   mysql -u root -p < db/init.sql
-- 或登录 MySQL 后执行：
--   source db/init.sql
-- ============================================================

-- ============================================================
-- PART 1: 建库
-- ============================================================
CREATE DATABASE IF NOT EXISTS blueseacode
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE blueseacode;

-- ============================================================
-- PART 2: 基础表结构（用户 + 角色 + 权限）
-- ============================================================

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    bio VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
    github VARCHAR(200) DEFAULT NULL COMMENT 'GitHub地址',
    website VARCHAR(200) DEFAULT NULL COMMENT '个人网站',
    score INT DEFAULT 0 COMMENT '积分',
    status TINYINT DEFAULT 0 COMMENT '状态: 0正常 1禁用',
    last_login_ip VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0正常 1删除',
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 权限/菜单表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT NULL COMMENT '父级ID',
    name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission VARCHAR(100) DEFAULT NULL COMMENT '权限标识符(如 sys:user:list)',
    path VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    component VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    type TINYINT NOT NULL COMMENT '类型: 0目录 1菜单 2按钮',
    icon VARCHAR(100) DEFAULT NULL COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0隐藏 1显示',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限/菜单表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_perm (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ============================================================
-- PART 3: AI 模块表结构（FAQ + 知识库 + 工单 + 对话历史）
-- ============================================================

-- FAQ表
CREATE TABLE IF NOT EXISTS chat_faq (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question VARCHAR(500) NOT NULL COMMENT '常见问题',
    answer TEXT NOT NULL COMMENT '答案',
    category VARCHAR(50) DEFAULT NULL COMMENT '分类: PLATFORM/TECH/OTHER',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0隐藏 1显示',
    hit_count INT DEFAULT 0 COMMENT '命中次数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='FAQ表';

-- 知识库表
CREATE TABLE IF NOT EXISTS chat_knowledge_base (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '知识标题',
    content TEXT NOT NULL COMMENT '知识内容',
    category VARCHAR(50) DEFAULT NULL COMMENT '分类',
    keywords VARCHAR(500) DEFAULT NULL COMMENT '关键词(逗号分隔)',
    status TINYINT DEFAULT 1 COMMENT '0隐藏 1显示',
    hit_count INT DEFAULT 0 COMMENT '命中次数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    FULLTEXT INDEX ft_keywords (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';

-- 工单表
CREATE TABLE IF NOT EXISTS chat_ticket (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '创建用户ID',
    title VARCHAR(200) NOT NULL COMMENT '工单标题',
    description TEXT COMMENT '问题描述',
    category VARCHAR(30) DEFAULT NULL COMMENT '分类: ACCOUNT/TECH/REPORT/OTHER',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING待处理/PROCESSING处理中/RESOLVED已解决/CLOSED已关闭',
    priority TINYINT DEFAULT 2 COMMENT '优先级: 1低 2中 3高 4紧急',
    assignee_id BIGINT DEFAULT NULL COMMENT '处理人(客服)ID',
    close_reason VARCHAR(500) DEFAULT NULL COMMENT '关闭原因',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user (user_id),
    INDEX idx_assignee (assignee_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单表';

-- 工单回复表
CREATE TABLE IF NOT EXISTS chat_ticket_reply (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL COMMENT '工单ID',
    user_id BIGINT NOT NULL COMMENT '回复人ID',
    content TEXT NOT NULL COMMENT '回复内容',
    is_staff TINYINT DEFAULT 0 COMMENT '是否客服回复: 0用户 1客服',
    attachments VARCHAR(1000) DEFAULT NULL COMMENT '附件(JSON数组)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ticket (ticket_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单回复表';

-- 对话历史表
CREATE TABLE IF NOT EXISTS chat_conversation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    session_id VARCHAR(100) NOT NULL COMMENT '会话ID',
    question TEXT NOT NULL COMMENT '用户问题',
    answer TEXT COMMENT 'AI回答',
    ai_model VARCHAR(50) DEFAULT 'OPENAI' COMMENT 'AI模型',
    answer_type VARCHAR(20) DEFAULT 'AI' COMMENT '回答类型: AI/FAQ/TRANSFER(转人工)',
    score TINYINT DEFAULT NULL COMMENT '用户评分:1-5',
    feedback TEXT COMMENT '用户反馈',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_session (session_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话历史表';

-- ============================================================
-- PART 4: 初始化数据
-- ============================================================

-- 初始化角色
INSERT INTO sys_role (role_code, role_name, description, sort) VALUES
('ROLE_ADMIN', '超级管理员', '系统最高权限', 1),
('ROLE_OPERATOR', '运营人员', '内容管理与运营', 2),
('ROLE_REVIEWER', '审核员', '内容审核', 3),
('ROLE_CUSTOMER_SERVICE', '客服人员', '客服处理', 4),
('ROLE_USER', '普通用户', '注册用户', 5);

-- 默认管理员: admin / admin123
-- 密码 BCrypt: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH
INSERT INTO sys_user (username, password, nickname, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 0);

-- 管理员绑定 ROLE_ADMIN 角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- ============================================================
-- PART 5: 登录日志表
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    ip VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
    device VARCHAR(200) DEFAULT NULL COMMENT '设备信息',
    result TINYINT NOT NULL COMMENT '结果: 0失败 1成功',
    fail_reason VARCHAR(200) DEFAULT NULL COMMENT '失败原因',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- ============================================================
-- PART 6: 资源分享模块表结构
-- ============================================================

-- 资源表
CREATE TABLE IF NOT EXISTS res_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '发布用户ID',
    title VARCHAR(200) NOT NULL COMMENT '资源标题',
    description TEXT COMMENT '资源描述(Markdown)',
    cover_image VARCHAR(500) DEFAULT NULL COMMENT '封面图URL',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    file_url VARCHAR(500) NOT NULL COMMENT '文件存储URL',
    file_name VARCHAR(200) DEFAULT NULL COMMENT '原始文件名',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    file_format VARCHAR(20) DEFAULT NULL COMMENT '文件格式',
    download_points INT DEFAULT 0 COMMENT '下载所需积分(0=免费)',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    collect_count INT DEFAULT 0 COMMENT '收藏数',
    status TINYINT DEFAULT 0 COMMENT '状态: 0待审 1通过 2驳回 3下架',
    reject_reason VARCHAR(500) DEFAULT NULL COMMENT '驳回原因',
    sort INT DEFAULT 0 COMMENT '排序(置顶用)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0正常 1删除',
    INDEX idx_user (user_id),
    INDEX idx_category (category_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    FULLTEXT INDEX ft_title_desc (title, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源表';

-- 资源分类表
CREATE TABLE IF NOT EXISTS res_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    parent_id BIGINT DEFAULT NULL COMMENT '父分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon VARCHAR(200) DEFAULT NULL COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源分类表';

-- 标签表
CREATE TABLE IF NOT EXISTS res_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- 资源-标签关联表
CREATE TABLE IF NOT EXISTS res_resource_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    UNIQUE KEY uk_res_tag (resource_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源标签关联表';

-- 资源评论表
CREATE TABLE IF NOT EXISTS res_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    user_id BIGINT NOT NULL COMMENT '评论人ID',
    content TEXT NOT NULL COMMENT '评论内容',
    parent_id BIGINT DEFAULT NULL COMMENT '回复的评论ID(楼中楼)',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    status TINYINT DEFAULT 1 COMMENT '状态: 0隐藏 1正常',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_resource (resource_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源评论表';

-- 资源收藏表
CREATE TABLE IF NOT EXISTS res_collect (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_res (user_id, resource_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源收藏表';

-- 初始分类数据
INSERT INTO res_category (parent_id, name, icon, sort) VALUES
(NULL, '视频教程', 'video-icon', 1),
(NULL, '电子书', 'book-icon', 2),
(NULL, '开发工具', 'tool-icon', 3),
(NULL, '代码模板', 'code-icon', 4),
(NULL, '项目源码', 'project-icon', 5),
(NULL, '面试题库', 'interview-icon', 6);

-- 初始标签数据
INSERT INTO res_tag (name) VALUES
('Java'),
('Python'),
('JavaScript'),
('TypeScript'),
('Vue.js'),
('React'),
('Spring Boot'),
('MySQL'),
('Redis'),
('Docker'),
('Kubernetes'),
('Git'),
('Linux'),
('算法'),
('数据结构'),
('设计模式'),
('微服务'),
('人工智能');
