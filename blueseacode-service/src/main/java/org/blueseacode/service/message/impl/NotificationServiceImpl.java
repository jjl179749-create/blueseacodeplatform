package org.blueseacode.service.message.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.blueseacode.common.response.PageResult;
import org.blueseacode.dao.entity.MsgNotification;
import org.blueseacode.dao.mapper.MsgNotificationMapper;
import org.blueseacode.dao.util.PageUtil;
import org.blueseacode.service.message.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final MsgNotificationMapper notificationMapper;

    @Override
    public void send(Long userId, String type, String title, String content,
                     Long relatedId, String relatedType) {
        MsgNotification notification = new MsgNotification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    @Override
    public void sendBatch(List<Long> userIds, String title, String content) {
        for (Long userId : userIds) {
            send(userId, "SYSTEM", title, content, null, null);
        }
    }

    @Override
    public PageResult<MsgNotification> list(Long userId, int page, int size, Boolean isRead) {
        Page<MsgNotification> pageResult = notificationMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<MsgNotification>()
                        .eq(MsgNotification::getUserId, userId)
                        .eq(isRead != null, MsgNotification::getIsRead, isRead != null && isRead ? 1 : 0)
                        .orderByDesc(MsgNotification::getCreateTime));
        return PageUtil.of(pageResult);
    }

    @Override
    public void markRead(Long notificationId, Long userId) {
        MsgNotification notification = new MsgNotification();
        notification.setId(notificationId);
        notification.setIsRead(1);
        notificationMapper.update(notification,
                new LambdaQueryWrapper<MsgNotification>()
                        .eq(MsgNotification::getId, notificationId)
                        .eq(MsgNotification::getUserId, userId));
    }

    @Override
    public void markAllRead(Long userId) {
        notificationMapper.markAllReadByUserId(userId);
    }

    @Override
    public long countUnread(Long userId) {
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<MsgNotification>()
                        .eq(MsgNotification::getUserId, userId)
                        .eq(MsgNotification::getIsRead, 0));
    }
}
