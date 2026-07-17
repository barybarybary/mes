package com.itheima.mes1.common.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通用消息信封 — 所有生产者和消费者共用此协议
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MqMessage {

    /** 事件类型，如 mail.send / alert.scan / audit.log / workorder.status.3 */
    private String eventType;

    /** 消息负载（JSON 字符串，消费者自行反序列化） */
    private String data;

    /** 消息产生时间 */
    private LocalDateTime timestamp;

    /** 链路追踪 ID，用于去重和日志关联（可选） */
    private String traceId;

    public MqMessage(String eventType, String data) {
        this.eventType = eventType;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
}
