package org.blueseacode.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_ticket")
public class ChatTicket extends BaseEntity {

    private Long userId;
    private String title;
    private String description;
    private String category;       // ACCOUNT/TECH/REPORT/OTHER
    private String status;         // PENDING/PROCESSING/RESOLVED/CLOSED
    private Integer priority;      // 1低 2中 3高 4紧急
    private Long assigneeId;
    private String closeReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;

    // ===== 非数据库字段 =====

    @TableField(exist = false)
    private String nickname;       // 创建者昵称

    @TableField(exist = false)
    private String assigneeName;   // 处理人昵称

    @TableField(exist = false)
    private List<ChatTicketReply> replies;  // 回复列表
}
