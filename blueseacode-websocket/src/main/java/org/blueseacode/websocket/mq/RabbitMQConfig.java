package org.blueseacode.websocket.mq;

import org.blueseacode.common.constant.AppConstant;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue notificationQueue() {
        return new Queue(AppConstant.QUEUE_NOTIFICATION, true);
    }

    @Bean
    public Queue pointsQueue() {
        return new Queue(AppConstant.QUEUE_POINTS, true);
    }

    @Bean
    public Queue logQueue() {
        return new Queue(AppConstant.QUEUE_LOG, true);
    }
}
