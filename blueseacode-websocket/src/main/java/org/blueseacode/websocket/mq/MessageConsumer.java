package org.blueseacode.websocket.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blueseacode.common.constant.AppConstant;
import org.blueseacode.dao.entity.SysOperationLog;
import org.blueseacode.dao.mapper.SysOperationLogMapper;
import org.blueseacode.service.message.NotificationService;
import org.blueseacode.service.points.PointsService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageConsumer {

    private final NotificationService notificationService;
    private final PointsService pointsService;
    private final SysOperationLogMapper logMapper;

    @RabbitListener(queues = AppConstant.QUEUE_NOTIFICATION)
    public void handleNotification(NotificationMessage msg) {
        log.info("处理通知消息: userId={}, type={}", msg.getUserId(), msg.getType());
        notificationService.send(msg.getUserId(), msg.getType(), msg.getTitle(),
                msg.getContent(), msg.getRelatedId(), msg.getRelatedType());
    }

    @RabbitListener(queues = AppConstant.QUEUE_POINTS)
    public void handlePoints(PointsMessage msg) {
        log.info("处理积分消息: userId={}, points={}, type={}", msg.getUserId(), msg.getPoints(), msg.getType());
        if ("ADD".equals(msg.getType())) {
            pointsService.add(msg.getUserId(), msg.getPoints(), msg.getReason());
        } else {
            pointsService.deduct(msg.getUserId(), msg.getPoints(), msg.getReason());
        }
    }

    @RabbitListener(queues = AppConstant.QUEUE_LOG)
    public void handleLog(OperationLogMessage msg) {
        log.info("处理日志消息: module={}, action={}", msg.getModule(), msg.getAction());
        SysOperationLog logRecord = new SysOperationLog();
        logRecord.setUserId(msg.getUserId());
        logRecord.setModule(msg.getModule());
        logRecord.setAction(msg.getAction());
        logRecord.setTargetType(msg.getTargetType());
        logRecord.setTargetId(msg.getTargetId());
        logRecord.setParams(msg.getParams());
        logRecord.setRequestIp(msg.getIp());
        logMapper.insert(logRecord);
    }
}
