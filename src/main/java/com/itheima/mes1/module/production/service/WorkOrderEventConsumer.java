package com.itheima.mes1.module.production.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.mes1.common.mq.MqMessage;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 工单事件消费者 — 监听 mes.alert.queue（routing key: notify.workorder.#）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkOrderEventConsumer {

    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "mes.alert.queue")
    public void onMessage(MqMessage message, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(message.getData(), Map.class);
            String eventType = message.getEventType();

            if (eventType != null && eventType.startsWith("notify.workorder.")) {
                Object orderId = payload.get("orderId");
                Object orderNo = payload.get("orderNo");
                Object status = payload.get("status");
                Object productName = payload.get("productName");

                log.info("工单事件 orderId={} orderNo={} status={} productName={} eventType={}",
                        orderId, orderNo, status, productName, eventType);

                // 未来扩展点：
                // - workorder.status.3 (已完成) → 自动创建质检任务
                // - workorder.status.4 (已入库) → 推送库存更新通知
                // - workorder.status.1 → status.2 (开始生产) → 通知物料员备料
            }

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("工单事件消费失败 eventType={}", message.getEventType(), e);
            try {
                channel.basicAck(deliveryTag, false);
            } catch (Exception ignored) {}
        }
    }
}
