package org.blueseacode.service.message;

import org.blueseacode.common.response.PageResult;
import org.blueseacode.dao.entity.MsgNotification;

import java.util.List;

public interface NotificationService {

    /** 发送通知 */
    void send(Long userId, String type, String title, String content,
              Long relatedId, String relatedType);

    /** 批量发送通知（如系统公告） */
    void sendBatch(List<Long> userIds, String title, String content);

    /** 获取用户通知列表 */
    PageResult<MsgNotification> list(Long userId, int page, int size, Boolean isRead);

    /** 标记单条已读 */
    void markRead(Long notificationId, Long userId);

    /** 一键已读 */
    void markAllRead(Long userId);

    /** 获取未读数量 */
    long countUnread(Long userId);
}
