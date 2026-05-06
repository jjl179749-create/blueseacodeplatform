package org.blueseacode.web.controller.portal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.common.response.Result;
import org.blueseacode.dao.entity.MsgPrivateMessage;
import org.blueseacode.websocket.service.PrivateMessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(AppConstant.PORTAL_PREFIX + "/messages")
@RequiredArgsConstructor
public class PrivateMessageController {

    private final PrivateMessageService messageService;

    /**
     * 发送私信
     */
    @PostMapping
    public Result<MsgPrivateMessage> send(@Valid @RequestBody SendMessageRequest req,
                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        MsgPrivateMessage msg = messageService.send(userId, req.getToUserId(), req.getContent());
        return Result.success(msg);
    }

    /**
     * 获取与某人的会话记录
     */
    @GetMapping("/conversation/{userId}")
    public Result<PageResult<MsgPrivateMessage>> conversation(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success(messageService.getConversation(currentUserId, userId, page, size));
    }

    /**
     * 获取最近联系人ID列表
     */
    @GetMapping("/contacts")
    public Result<List<Long>> contacts(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success(messageService.getRecentContacts(userId));
    }

    /**
     * 获取未读私信数
     */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        return Result.success(messageService.countUnread(userId));
    }

    /**
     * 标记单条消息已读
     */
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        messageService.markAsRead(id, userId);
        return Result.success();
    }

    /**
     * 标记与某人的会话全部已读
     */
    @PutMapping("/read-conversation/{userId}")
    public Result<Void> markConversationRead(@PathVariable Long userId,
                                             HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute(AppConstant.CURRENT_USER_ID);
        messageService.markConversationRead(currentUserId, userId);
        return Result.success();
    }

    // ===== 请求体 =====

    @Data
    public static class SendMessageRequest {
        @NotNull(message = "接收者不能为空")
        private Long toUserId;
        @NotBlank(message = "消息内容不能为空")
        private String content;
    }
}
