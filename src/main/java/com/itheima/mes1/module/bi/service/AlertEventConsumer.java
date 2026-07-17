package com.itheima.mes1.module.bi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.mes1.common.mq.MqMessage;
import com.itheima.mes1.module.bi.controller.BiSseController;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * BI 告警事件消费者 — 监听 mes.alert.queue，广播 SSE
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertEventConsumer {

    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "mes.alert.queue")
    public void onMessage(MqMessage message, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(message.getData(), Map.class);

            if ("alert.scan".equals(message.getEventType())) {
                Object unreadObj = payload.get("unread");
                long unread = unreadObj instanceof Number n ? n.longValue() : 0;
                BiSseController.broadcast(unread);
                log.debug("SSE广播告警未读数 unread={}", unread);
            }

            // 无需手动 ack — 告警消息无需 DLQ，自动确认即可
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("告警事件消费失败 eventType={}", message.getEventType(), e);
            try {
                channel.basicAck(deliveryTag, false); // 即使失败也 ack，避免死循环
            } catch (Exception ignored) {}
        }
    }
}
