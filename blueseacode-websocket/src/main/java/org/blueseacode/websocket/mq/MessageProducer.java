package org.blueseacode.websocket.mq;

import lombok.RequiredArgsConstructor;
import org.blueseacode.common.constant.AppConstant;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    /** 发送通知消息 */
    public void sendNotification(NotificationMessage msg) {
        rabbitTemplate.convertAndSend("", AppConstant.QUEUE_NOTIFICATION, msg);
    }

    /** 发送积分变动消息 */
    public void sendPointsChange(PointsMessage msg) {
        rabbitTemplate.convertAndSend("", AppConstant.QUEUE_POINTS, msg);
    }

    /** 发送日志消息 */
    public void sendLog(OperationLogMessage msg) {
        rabbitTemplate.convertAndSend("", AppConstant.QUEUE_LOG, msg);
    }
}
