package org.blueseacode.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    private Long id;
    private Long userId;
    private String username;
    private String module;
    private String action;
    private Long targetId;
    private String targetType;
    private String requestIp;
    private String params;
    private String result;
    private Long duration;
    private LocalDateTime createTime;
}
