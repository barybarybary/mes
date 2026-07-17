package com.itheima.mes1.module.system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.mes1.common.mq.MqAckUtil;
import com.itheima.mes1.common.mq.MqMessage;
import com.itheima.mes1.module.system.entity.OperationLog;
import com.itheima.mes1.module.system.mapper.OperationLogMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 审计日志消费者 — 监听 mes.audit.queue
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogConsumer {

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "mes.audit.queue")
    public void onMessage(MqMessage message, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(message.getData(), Map.class);

            OperationLog logEntry = new OperationLog();
            logEntry.setOperatorId(toLong(payload.get("operatorId")));
            logEntry.setOperation((String) payload.get("operation"));
            logEntry.setTargetType((String) payload.get("targetType"));
            logEntry.setTargetId(toLong(payload.get("targetId")));
            logEntry.setDetail((String) payload.get("detail"));
            logEntry.setIp((String) payload.get("ip"));

            operationLogMapper.insert(logEntry);
            log.debug("审计日志已持久化 operation={} targetType={} targetId={}",
                    logEntry.getOperation(), logEntry.getTargetType(), logEntry.getTargetId());

            MqAckUtil.ack(channel, deliveryTag);
        } catch (Exception e) {
            log.error("审计日志消费失败 eventType={}", message.getEventType(), e);
            MqAckUtil.nack(channel, deliveryTag);
        }
    }

    private Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number n) return n.longValue();
        try {
            return Long.valueOf(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
