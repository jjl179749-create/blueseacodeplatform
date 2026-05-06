package org.blueseacode.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_faq")
public class ChatFaq extends BaseEntity {

    private String question;
    private String answer;
    private String category;       // PLATFORM/TECH/OTHER
    private Integer sort;
    private Integer status;        // 0隐藏 1显示
    private Integer hitCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
