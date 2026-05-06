package org.blueseacode.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_conversation")
public class ChatConversation extends BaseEntity {

    private Long userId;
    private String sessionId;
    private String question;
    private String answer;
    private String aiModel;        // VOLC_ENGINE
    private String answerType;     // AI/FAQ/TRANSFER
    private Integer score;         // 用户评分 1-5
    private String feedback;
    private LocalDateTime createTime;
}
