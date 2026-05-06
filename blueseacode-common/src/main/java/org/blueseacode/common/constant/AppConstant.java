package org.blueseacode.common.constant;

public interface AppConstant {

    /** 用户端API前缀 */
    String PORTAL_PREFIX = "/api/portal";

    /** 管理端API前缀 */
    String ADMIN_PREFIX = "/api/admin";

    /** Token 请求头 */
    String AUTH_HEADER = "Authorization";

    /** Token 前缀 */
    String TOKEN_PREFIX = "Bearer ";

    /** 当前用户ID (Request属性键) */
    String CURRENT_USER_ID = "currentUserId";

    /** 当前用户角色 (Request属性键) */
    String CURRENT_USER_ROLES = "currentUserRoles";

    // ===== 缓存Key前缀 =====
    String CACHE_USER = "user:";
    String CACHE_TOKEN_BLACKLIST = "token:blacklist:";
    String CACHE_CAPTCHA = "captcha:";

    // ===== RabbitMQ 队列 =====
    String QUEUE_NOTIFICATION = "queue.notification";
    String QUEUE_POINTS = "queue.points";
    String QUEUE_LOG = "queue.log";
}
