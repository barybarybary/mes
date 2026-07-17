package com.itheima.mes1.common.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * 消息发送工具 — 所有业务代码通过此类发送 MQ 消息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSender {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        // 发布确认回调：记录投递失败的消息
        rabbitTemplate.setConfirmCallback((CorrelationData correlationData, boolean ack, String cause) -> {
            if (!ack) {
                log.warn("消息投递失败 correlationId={} cause={}", correlationData != null ? correlationData.getId() : null, cause);
            }
        });
        // 退回回调：记录无法路由的消息
        rabbitTemplate.setReturnsCallback(returned -> {
            log.warn("消息无法路由 exchange={} routingKey={} replyText={}",
                    returned.getExchange(), returned.getRoutingKey(), returned.getReplyText());
        });
    }

    // ========== 通用发送方法 ==========

    /** 发送通用消息到指定交换机和路由键 */
    public void send(String exchange, String routingKey, MqMessage message) {
        if (message.getTraceId() == null) {
            message.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        }
        CorrelationData correlationData = new CorrelationData(message.getTraceId());
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
            log.debug("消息已发送 exchange={} routingKey={} eventType={} traceId={}",
                    exchange, routingKey, message.getEventType(), message.getTraceId());
        } catch (Exception e) {
            log.warn("消息发送失败（RabbitMQ 不可用） exchange={} routingKey={} eventType={}: {}",
                    exchange, routingKey, message.getEventType(), e.getMessage());
        }
    }

    // ========== 业务便捷方法 ==========

    /**
     * 发送邮件 — 异步消费后由 MailQueueConsumer 调用 MailService
     * @param to      收件人
     * @param subject 邮件主题
     * @param html    邮件 HTML 正文
     */
    public void sendMail(String to, String subject, String html) {
        Map<String, String> payload = Map.of("to", to, "subject", subject, "html", html);
        MqMessage msg = new MqMessage("mail.send", toJson(payload));
        send("mes.mail.exchange", "mail.send", msg);
    }

    /**
     * 发送验证码邮件
     * @param to   收件人
     * @param code 验证码
     */
    public void sendVerifyCodeMail(String to, String code) {
        Map<String, String> payload = Map.of("to", to, "mode", "verifyCode", "code", code);
        MqMessage msg = new MqMessage("mail.send", toJson(payload));
        send("mes.mail.exchange", "mail.send", msg);
    }

    /**
     * 发送带附件的 HTML 邮件
     * @param to              收件人
     * @param subject         邮件主题
     * @param html            邮件 HTML 正文
     * @param attachment      附件字节数组
     * @param attachmentName  附件文件名
     */
    public void sendMailWithAttachment(String to, String subject, String html,
                                        byte[] attachment, String attachmentName) {
        Map<String, String> payload = new java.util.HashMap<>();
        payload.put("to", to);
        payload.put("subject", subject);
        payload.put("html", html);
        payload.put("mode", "attachment");
        payload.put("attachmentBase64", Base64.getEncoder().encodeToString(attachment));
        payload.put("attachmentName", attachmentName);
        MqMessage msg = new MqMessage("mail.send", toJson(payload));
        send("mes.mail.exchange", "mail.send", msg);
    }

    /**
     * 发送业务事件（告警、工单变更等）
     * @param routingKey 路由键，如 alert.scan / notify.workorder.3
     * @param payload    事件数据
     */
    public void sendEvent(String routingKey, Map<String, Object> payload) {
        MqMessage msg = new MqMessage(routingKey, toJson(payload));
        send("mes.event.exchange", routingKey, msg);
    }

    /**
     * 发送审计日志
     * @param operatorId 操作人 ID（可为 null）
     * @param operation  操作类型 CREATE/UPDATE/DELETE/LOGIN/EXPORT
     * @param targetType 目标类型 WorkOrder/SaleOrder/Product
     * @param targetId   目标 ID（可为 null）
     * @param detail     变更详情 JSON
     * @param ip         客户端 IP
     */
    public void sendAudit(Long operatorId, String operation, String targetType,
                          Long targetId, String detail, String ip) {
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("operatorId", operatorId);
        payload.put("operation", operation);
        payload.put("targetType", targetType);
        payload.put("targetId", targetId);
        payload.put("detail", detail);
        payload.put("ip", ip);
        MqMessage msg = new MqMessage("audit.log", toJson(payload));
        send("mes.audit.exchange", "audit.log", msg);
    }

    // ========== 内部工具 ==========

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return "{}";
        }
    }
}
