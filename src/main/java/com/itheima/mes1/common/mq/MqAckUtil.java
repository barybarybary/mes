package com.itheima.mes1.common.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 * RabbitMQ 手动确认工具 — 避免消费者中重复的 try-catch
 */
@Slf4j
public final class MqAckUtil {

    private MqAckUtil() {}

    /** 确认消息处理成功，从队列中移除 */
    public static void ack(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("消息确认失败 deliveryTag={}", deliveryTag, e);
        }
    }

    /** 拒绝消息且不重新入队（消息进入 DLQ） */
    public static void nack(Channel channel, long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, false);
        } catch (Exception e) {
            log.error("消息拒绝失败 deliveryTag={}", deliveryTag, e);
        }
    }
}
