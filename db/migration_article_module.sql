-- ============================================================
-- 编程提升(文章)模块 - 增量迁移脚本
-- 适用场景：已在 blueseacode 数据库运行过 init.sql 的情况下，
--           单独为 Phase 5 文章模块建表 + 初始化数据
-- 安全特性：所有 CREATE 使用 IF NOT EXISTS
--           所有 INSERT 使用 INSERT IGNORE，可重复执行
-- ============================================================

USE blueseacode;

-- ========== 文章表 ==========
CREATE TABLE IF NOT EXISTS art_article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '作者ID',
    title VARCHAR(200) NOT NULL COMMENT '文章标题',
    summary VARCHAR(500) DEFAULT NULL COMMENT '文章摘要',
    cover_image VARCHAR(500) DEFAULT NULL COMMENT '封面图',
    content LONGTEXT COMMENT '文章内容(Markdown)',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    view_count INT DEFAULT 0 COMMENT '阅读量',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    collect_count INT DEFAULT 0 COMMENT '收藏数',
    is_comment TINYINT DEFAULT 1 COMMENT '允许评论: 0否 1是',
    status TINYINT DEFAULT 0 COMMENT '状态: 0草稿 1待审 2通过 3驳回',
    reject_reason VARCHAR(500) DEFAULT NULL COMMENT '驳回原因',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0正常 1删除',
    INDEX idx_user (user_id),
    INDEX idx_category (category_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    FULLTEXT INDEX ft_title (title, summary)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- ========== 文章分类表 ==========
CREATE TABLE IF NOT EXISTS art_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon VARCHAR(200) DEFAULT NULL COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0正常 1删除',
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章分类表';

-- ========== 文章标签表 ==========
CREATE TABLE IF NOT EXISTS art_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签表';

-- ========== 文章-标签关联表 ==========
CREATE TABLE IF NOT EXISTS art_article_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL COMMENT '文章ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    UNIQUE KEY uk_art_tag (article_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签关联表';

-- ========== 文章评论表 ==========
CREATE TABLE IF NOT EXISTS art_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    user_id BIGINT NOT NULL COMMENT '评论人ID',
    content TEXT NOT NULL COMMENT '评论内容',
    parent_id BIGINT DEFAULT NULL COMMENT '回复的评论ID(楼中楼)',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    status TINYINT DEFAULT 1 COMMENT '状态: 0隐藏 1正常',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0正常 1删除',
    INDEX idx_article (article_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章评论表';

-- ========== 文章点赞表 ==========
CREATE TABLE IF NOT EXISTS art_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_art (user_id, article_id),
    INDEX idx_user (user_id),
    INDEX idx_article (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章点赞表';

-- ========== 文章收藏表 ==========
CREATE TABLE IF NOT EXISTS art_collect (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_collect (user_id, article_id),
    INDEX idx_user (user_id),
    INDEX idx_article (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章收藏表';

-- ========== 初始化数据 ==========

-- 文章分类（可重复执行）
INSERT IGNORE INTO art_category (name, icon, sort) VALUES
('Java', 'java-icon', 1),
('Python', 'python-icon', 2),
('前端开发', 'frontend-icon', 3),
('数据库', 'database-icon', 4),
('算法与数据结构', 'algorithm-icon', 5),
('架构设计', 'architecture-icon', 6),
('职业成长', 'career-icon', 7),
('其他', 'other-icon', 8);

-- 文章标签（可重复执行）
INSERT IGNORE INTO art_tag (name) VALUES
('Java'),
('Spring Boot'),
('微服务'),
('MySQL'),
('Redis'),
('消息队列'),
('分布式'),
('前端'),
('Vue.js'),
('React'),
('TypeScript'),
('Python'),
('人工智能'),
('算法'),
('数据结构'),
('设计模式'),
('DevOps'),
('Linux'),
('职业成长'),
('面试');

