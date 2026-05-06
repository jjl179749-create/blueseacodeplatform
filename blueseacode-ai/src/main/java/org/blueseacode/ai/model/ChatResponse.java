package org.blueseacode.ai.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
public class ChatResponse {

    private String type;              // FAQ / KNOWLEDGE / AI / TRANSFER
    private String content;
    private List<String> suggestions; // 快捷问题建议
    private boolean canTransfer;      // 是否可转人工
    private String sessionId;         // 当前会话ID

    public static ChatResponse faq(String content, String sessionId) {
        return new ChatResponse("FAQ", content,
                Arrays.asList("积分怎么获取？", "如何发布资源？", "怎么联系客服？"),
                true, sessionId);
    }

    public static ChatResponse knowledge(String content, String sessionId) {
        return new ChatResponse("KNOWLEDGE", content, null, true, sessionId);
    }

    public static ChatResponse ai(String content, String sessionId) {
        return new ChatResponse("AI", content,
                Arrays.asList("积分怎么获取？", "如何发布资源？", "怎么创建工单？"),
                true, sessionId);
    }
}
