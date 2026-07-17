package com.itheima.mes1.module.system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.mes1.common.MailService;
import com.itheima.mes1.common.mq.MqAckUtil;
import com.itheima.mes1.common.mq.MqMessage;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;

/**
 * 异步邮件消费者 — 监听 mes.mail.queue
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MailQueueConsumer {

    private final MailService mailService;
    private final ObjectMapper objectMapper;

    /**
     * 消费邮件消息，使用 @Async 释放 MQ 监听线程
     */
    @Async("mailTaskExecutor")
    @RabbitListener(queues = "mes.mail.queue")
    public void onMessage(MqMessage message, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            Map<String, String> payload = objectMapper.readValue(message.getData(), Map.class);
            String mode = payload.getOrDefault("mode", "html");

            if ("verifyCode".equals(mode)) {
                String to = payload.get("to");
                String code = payload.get("code");
                mailService.sendVerifyCode(to, code);
                log.info("验证码邮件异步发送成功 to={}", to);
            } else if ("attachment".equals(mode)) {
                String to = payload.get("to");
                String subject = payload.get("subject");
                String html = payload.get("html");
                String base64 = payload.get("attachmentBase64");
                String filename = payload.get("attachmentName");
                byte[] attachment = Base64.getDecoder().decode(base64);
                mailService.sendHtmlWithAttachment(to, subject, html, attachment, filename);
                log.info("带附件邮件异步发送成功 to={} subject={} filename={}", to, subject, filename);
            } else {
                String to = payload.get("to");
                String subject = payload.get("subject");
                String html = payload.get("html");
                mailService.sendHtml(to, subject, html);
                log.info("HTML邮件异步发送成功 to={} subject={}", to, subject);
            }

            MqAckUtil.ack(channel, deliveryTag);
        } catch (Exception e) {
            log.error("邮件消费失败 eventType={}", message.getEventType(), e);
            MqAckUtil.nack(channel, deliveryTag);
        }
    }
}
