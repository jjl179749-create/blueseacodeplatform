package org.blueseacode.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.blueseacode.common.enums.ResultCode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    private Integer code;
    private String message;
    private T data;
    private String timestamp;

    private Result() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // ========== 成功响应 ==========
    public static <T> Result<T> success() {
        Result<T> r = new Result<>();
        r.code = ResultCode.SUCCESS.getCode();
        r.message = ResultCode.SUCCESS.getMessage();
        return r;
    }

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = ResultCode.SUCCESS.getCode();
        r.message = ResultCode.SUCCESS.getMessage();
        r.data = data;
        return r;
    }

    public static <T> Result<T> success(String message) {
        Result<T> r = new Result<>();
        r.code = ResultCode.SUCCESS.getCode();
        r.message = message;
        return r;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> r = new Result<>();
        r.code = ResultCode.SUCCESS.getCode();
        r.message = message;
        r.data = data;
        return r;
    }

    // ========== 失败响应 ==========
    public static <T> Result<T> failed() {
        Result<T> r = new Result<>();
        r.code = ResultCode.FAILED.getCode();
        r.message = ResultCode.FAILED.getMessage();
        return r;
    }

    public static <T> Result<T> failed(String message) {
        Result<T> r = new Result<>();
        r.code = ResultCode.FAILED.getCode();
        r.message = message;
        return r;
    }

    public static <T> Result<T> failed(ResultCode resultCode) {
        Result<T> r = new Result<>();
        r.code = resultCode.getCode();
        r.message = resultCode.getMessage();
        return r;
    }

    public static <T> Result<T> failed(Integer code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }

    // ========== 便捷工厂 ==========
    public static <T> Result<T> unauthorized(String message) {
        return failed(ResultCode.UNAUTHORIZED.getCode(), message);
    }

    public static <T> Result<T> forbidden(String message) {
        return failed(ResultCode.FORBIDDEN.getCode(), message);
    }

    public static <T> Result<T> notFound() {
        return failed(ResultCode.NOT_FOUND);
    }

    public static <T> Result<T> validateFailed(String message) {
        return failed(ResultCode.VALIDATE_FAILED.getCode(), message);
    }
}
