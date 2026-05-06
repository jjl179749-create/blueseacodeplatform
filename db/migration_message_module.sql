-- ============================================================
-- 蓝海编程平台 - 实时消息与通知模块 (Phase 9)
-- 对应模块: blueseacode-websocket + blueseacode-service.message
-- ============================================================

CREATE TABLE IF NOT EXISTS msg_private_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    from_user_id BIGINT NOT NULL COMMENT '发送者ID',
    to_user_id BIGINT NOT NULL COMMENT '接收者ID',
    content TEXT NOT NULL COMMENT '消息内容',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_from (from_user_id),
    INDEX idx_to (to_user_id, is_read),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信表';

-- 用户关注表
CREATE TABLE IF NOT EXISTS msg_user_follow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    follower_id BIGINT NOT NULL COMMENT '关注者ID',
    followee_id BIGINT NOT NULL COMMENT '被关注者ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_follow (follower_id, followee_id),
    INDEX idx_follower (follower_id),
    INDEX idx_followee (followee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注表';

-- 消息通知表（评论、点赞、审核结果、新粉丝等系统通知）
CREATE TABLE IF NOT EXISTS msg_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    type VARCHAR(30) NOT NULL COMMENT '类型: SYSTEM/LIKE/COMMENT/AUDIT/FOLLOW',
    title VARCHAR(200) NOT NULL COMMENT '消息标题',
    content TEXT COMMENT '消息内容',
    related_id BIGINT DEFAULT NULL COMMENT '关联业务ID',
    related_type VARCHAR(30) DEFAULT NULL COMMENT '关联业务类型: RESOURCE/ARTICLE/DEMAND/USER',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0正常 1删除',
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

