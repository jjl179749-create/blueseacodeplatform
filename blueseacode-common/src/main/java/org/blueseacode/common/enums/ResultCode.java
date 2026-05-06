package org.blueseacode.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "success"),
    FAILED(400, "请求失败"),
    VALIDATE_FAILED(40001, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "没有权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    // ===== 业务异常 40100-40999 =====
    USER_NOT_FOUND(40100, "用户不存在"),
    USER_DISABLED(40101, "账号已被禁用"),
    PASSWORD_ERROR(40102, "密码错误"),
    USERNAME_EXISTS(40103, "用户名已存在"),
    OLD_PASSWORD_ERROR(40104, "原密码错误"),
    TOKEN_INVALID(40105, "Token无效或已过期"),
    TOKEN_REFRESH_FAILED(40106, "Token刷新失败"),

    RESOURCE_NOT_FOUND(40200, "资源不存在"),
    RESOURCE_DOWNLOAD_FAILED(40201, "下载失败，积分不足"),
    RESOURCE_AUDIT_FAILED(40202, "资源审核不通过"),

    ARTICLE_NOT_FOUND(40300, "文章不存在"),
    DEMAND_NOT_FOUND(40400, "需求不存在"),
    DEMAND_ALREADY_TAKEN(40401, "该需求已被接单"),
    DEMAND_NOT_OPEN(40402, "当前需求状态不允许操作"),

    POINTS_INSUFFICIENT(40500, "积分不足"),
    FILE_TOO_LARGE(40600, "文件大小超过限制"),
    FILE_TYPE_NOT_ALLOWED(40601, "文件类型不允许"),

    SYSTEM_ERROR(50000, "系统内部错误"),
    OPERATION_FAILED(50001, "操作失败，请稍后重试"),
    ;

    private final Integer code;
    private final String message;
}
