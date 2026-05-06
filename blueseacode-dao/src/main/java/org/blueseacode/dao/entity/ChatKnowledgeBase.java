package org.blueseacode.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_knowledge_base")
public class ChatKnowledgeBase extends BaseEntity {

    private String title;
    private String content;
    private String category;
    private String keywords;       // 逗号分隔
    private Integer status;        // 0隐藏 1显示
    private Integer hitCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
