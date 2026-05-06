package org.blueseacode.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_ticket_reply")
public class ChatTicketReply extends BaseEntity {

    private Long ticketId;
    private Long userId;
    private String content;
    private Integer isStaff;       // 0用户 1客服
    private String attachments;    // JSON数组
    private LocalDateTime createTime;
}
