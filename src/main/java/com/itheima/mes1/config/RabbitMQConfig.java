package com.itheima.mes1.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置 — 交换机、队列、绑定、DLQ、JSON 消息转换器
 */
@Configuration
public class RabbitMQConfig {

    // ========== 交换机定义 ==========

    @Bean
    public DirectExchange mailExchange() {
        return new DirectExchange("mes.mail.exchange", true, false);
    }

    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange("mes.event.exchange", true, false);
    }

    @Bean
    public DirectExchange auditExchange() {
        return new DirectExchange("mes.audit.exchange", true, false);
    }

    // ========== 队列定义 ==========

    /** 邮件队列 — 带 DLQ */
    @Bean
    public Queue mailQueue() {
        return QueueBuilder.durable("mes.mail.queue")
                .deadLetterExchange("")
                .deadLetterRoutingKey("mes.mail.dlq")
                .build();
    }

    /** 邮件死信队列 */
    @Bean
    public Queue mailDeadLetterQueue() {
        return QueueBuilder.durable("mes.mail.dlq").build();
    }

    /** 告警/事件队列 — 无 DLQ（下次 cron 扫描会重新生成） */
    @Bean
    public Queue alertQueue() {
        return QueueBuilder.durable("mes.alert.queue").build();
    }

    /** 审计日志队列 — 带 DLQ */
    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable("mes.audit.queue")
                .deadLetterExchange("")
                .deadLetterRoutingKey("mes.audit.dlq")
                .build();
    }

    /** 审计日志死信队列 */
    @Bean
    public Queue auditDeadLetterQueue() {
        return QueueBuilder.durable("mes.audit.dlq").build();
    }

    // ========== 绑定关系 ==========

    @Bean
    public Binding mailBinding() {
        return BindingBuilder.bind(mailQueue()).to(mailExchange()).with("mail.send");
    }

    @Bean
    public Binding alertBinding() {
        return BindingBuilder.bind(alertQueue()).to(eventExchange()).with("alert.*");
    }

    @Bean
    public Binding notifyBinding() {
        return BindingBuilder.bind(alertQueue()).to(eventExchange()).with("notify.#");
    }

    @Bean
    public Binding auditBinding() {
        return BindingBuilder.bind(auditQueue()).to(auditExchange()).with("audit.log");
    }

    // ========== 消息转换器 ==========

    /** JSON 序列化 — 替代默认的 Java 序列化，使消息在 RabbitMQ 管理界面可见 */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
