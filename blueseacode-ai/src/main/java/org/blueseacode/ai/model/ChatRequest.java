package org.blueseacode.ai.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {

    private Long userId;

    private String sessionId;    // 会话ID（前端生成UUID），空则创建新会话

    @NotBlank(message = "消息不能为空")
    private String question;
}
