package org.blueseacode.web.controller.portal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.blueseacode.ai.chat.AIChatService;
import org.blueseacode.ai.model.ChatRequest;
import org.blueseacode.ai.model.ChatResponse;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.entity.ChatConversation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(AppConstant.PORTAL_PREFIX + "/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AIChatService chatService;

    /** 发送消息给AI */
    @PostMapping("/send")
    public Result<ChatResponse> send(@Valid @RequestBody ChatRequest request,
                                      HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(AppConstant.CURRENT_USER_ID);
        request.setUserId(userId);
        return Result.success(chatService.processMessage(request));
    }

    /** 获取对话历史（可按sessionId筛选） */
    @GetMapping("/history")
    public Result<List<ChatConversation>> history(
            @RequestParam(required = false) String sessionId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success(chatService.getHistory(userId, sessionId));
    }
}
