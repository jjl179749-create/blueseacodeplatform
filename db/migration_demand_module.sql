-- ===== 需求发布模块 - 迁移脚本 =====
-- 说明：本脚本可重复执行（CREATE 使用 IF NOT EXISTS，INSERT 使用 IGNORE）
-- 对应文档：技术文档-05-需求发布模块（竞标模式 → 接单模式）
-- 变更：去掉 dem_bid 表，新增 dem_order 表（记录谁接了单）

-- ========== 需求表 ==========
CREATE TABLE IF NOT EXISTS dem_demand (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '发布者ID',
    title VARCHAR(200) NOT NULL COMMENT '需求标题',
    description TEXT COMMENT '需求描述(Markdown)',
    category VARCHAR(30) NOT NULL COMMENT '分类: PROJECT/TECH_CONSULT/TEAM_UP/OTHER',
    budget_min DECIMAL(10,2) DEFAULT NULL COMMENT '预算下限',
    budget_max DECIMAL(10,2) DEFAULT NULL COMMENT '预算上限',
    deadline DATETIME DEFAULT NULL COMMENT '截止日期',
    contact VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
    view_count INT DEFAULT 0 COMMENT '浏览数',
    order_count INT DEFAULT 0 COMMENT '接单数',
    taker_id BIGINT DEFAULT NULL COMMENT '接单用户ID',
    status TINYINT DEFAULT 0 COMMENT '状态: 0待审 1招募中 2进行中 3已完成 4已关闭 5驳回',
    reject_reason VARCHAR(500) DEFAULT NULL COMMENT '驳回原因',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user (user_id),
    INDEX idx_taker (taker_id),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求表';

-- ========== 需求附件表 ==========
CREATE TABLE IF NOT EXISTS dem_attachment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    demand_id BIGINT NOT NULL COMMENT '需求ID',
    file_name VARCHAR(200) NOT NULL COMMENT '文件名',
    file_url VARCHAR(500) NOT NULL COMMENT '文件URL',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_demand (demand_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求附件表';

-- ========== 接单记录表（替代原竞标表） ==========
CREATE TABLE IF NOT EXISTS dem_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    demand_id BIGINT NOT NULL COMMENT '需求ID',
    user_id BIGINT NOT NULL COMMENT '接单用户ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_demand_user (demand_id, user_id),
    INDEX idx_demand (demand_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接单记录表';

-- ========== 需求进度表 ==========
CREATE TABLE IF NOT EXISTS dem_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    demand_id BIGINT NOT NULL COMMENT '需求ID',
    content TEXT NOT NULL COMMENT '进度内容',
    create_by BIGINT NOT NULL COMMENT '创建者ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_demand (demand_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求进度表';
