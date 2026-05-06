package org.blueseacode.websocket.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.dao.entity.MsgPrivateMessage;
import org.blueseacode.dao.mapper.MsgPrivateMessageMapper;
import org.blueseacode.dao.util.PageUtil;
import org.blueseacode.websocket.handler.UserSessionManager;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrivateMessageService {

    private final MsgPrivateMessageMapper messageMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserSessionManager sessionManager;

    /**
     * 发送私信（实时推送 + 持久化）
     */
    @Transactional
    public MsgPrivateMessage send(Long fromUserId, Long toUserId, String content) {
        // 1. 持久化
        MsgPrivateMessage msg = new MsgPrivateMessage();
        msg.setFromUserId(fromUserId);
        msg.setToUserId(toUserId);
        msg.setContent(content);
        msg.setIsRead(0);
        messageMapper.insert(msg);

        // 2. 实时推送给接收方（如果在线）
        if (sessionManager.isOnline(toUserId)) {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(toUserId),
                    "/queue/private",
                    msg);
        }

        return msg;
    }

    /**
     * 获取会话列表（两个用户之间的消息）
     */
    public PageResult<MsgPrivateMessage> getConversation(Long userId, Long otherUserId, int page, int size) {
        Page<MsgPrivateMessage> p = messageMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<MsgPrivateMessage>()
                        .and(w -> w.eq(MsgPrivateMessage::getFromUserId, userId)
                                .eq(MsgPrivateMessage::getToUserId, otherUserId))
                        .or(w -> w.eq(MsgPrivateMessage::getFromUserId, otherUserId)
                                .eq(MsgPrivateMessage::getToUserId, userId))
                        .orderByDesc(MsgPrivateMessage::getCreateTime));
        return PageUtil.of(p);
    }

    /**
     * 获取用户的最近联系人ID列表
     */
    public List<Long> getRecentContacts(Long userId) {
        return messageMapper.selectRecentContactUserIds(userId);
    }

    /**
     * 标记消息已读
     */
    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        messageMapper.update(null,
                new LambdaUpdateWrapper<MsgPrivateMessage>()
                        .eq(MsgPrivateMessage::getId, messageId)
                        .eq(MsgPrivateMessage::getToUserId, userId)
                        .set(MsgPrivateMessage::getIsRead, 1));
    }

    /**
     * 批量标记已读（与某人的所有消息）
     */
    @Transactional
    public void markConversationRead(Long userId, Long otherUserId) {
        messageMapper.update(null,
                new LambdaUpdateWrapper<MsgPrivateMessage>()
                        .eq(MsgPrivateMessage::getFromUserId, otherUserId)
                        .eq(MsgPrivateMessage::getToUserId, userId)
                        .eq(MsgPrivateMessage::getIsRead, 0)
                        .set(MsgPrivateMessage::getIsRead, 1));
    }

    /**
     * 获取未读消息数
     */
    public long countUnread(Long userId) {
        return messageMapper.selectCount(
                new LambdaQueryWrapper<MsgPrivateMessage>()
                        .eq(MsgPrivateMessage::getToUserId, userId)
                        .eq(MsgPrivateMessage::getIsRead, 0));
    }
}
