package org.blueseacode.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_login_log")
public class SysLoginLog {
    private Long id;
    private Long userId;
    private String username;
    private String ip;
    private String device;
    private Integer result;
    private String failReason;
    private LocalDateTime createTime;
}
