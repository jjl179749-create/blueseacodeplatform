package org.blueseacode.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("msg_private_message")
public class MsgPrivateMessage extends BaseEntity {

    private Long fromUserId;
    private Long toUserId;
    private String content;
    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // ===== 非数据库字段 =====
    @TableField(exist = false)
    private String fromNickname;

    @TableField(exist = false)
    private String fromAvatar;
}
