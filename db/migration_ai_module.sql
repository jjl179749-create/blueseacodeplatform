-- ========== AI智能客服模块 - 数据库迁移脚本 ==========
-- 对应 Phase 7：AI智能客服模块

-- ========== FAQ表 ==========
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

-- ========== 知识库表 ==========
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

-- ========== 工单表 ==========
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

-- ========== 工单回复表 ==========
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

-- ========== 对话历史表 ==========
CREATE TABLE IF NOT EXISTS chat_conversation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    session_id VARCHAR(100) NOT NULL COMMENT '会话ID',
    question TEXT NOT NULL COMMENT '用户问题',
    answer TEXT COMMENT 'AI回答',
    ai_model VARCHAR(50) DEFAULT 'VOLC_ENGINE' COMMENT 'AI模型',
    answer_type VARCHAR(20) DEFAULT 'AI' COMMENT '回答类型: AI/FAQ/TRANSFER(转人工)',
    score TINYINT DEFAULT NULL COMMENT '用户评分:1-5',
    feedback TEXT COMMENT '用户反馈',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_session (session_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话历史表';
