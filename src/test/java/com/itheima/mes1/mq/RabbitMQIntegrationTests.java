package com.itheima.mes1.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.itheima.mes1.common.mq.MqMessage;
import com.itheima.mes1.config.RabbitMQConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RabbitMQ 消息队列集成测试
 * 不需要 MySQL、Redis、RabbitMQ 可用 — 仅测试 POJO、序列化、配置
 */
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
                "org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration"
})
@ActiveProfiles("test")
@Import(RabbitMQIntegrationTests.MqTestConfig.class)
class RabbitMQIntegrationTests {

    /**
     * 轻量配置 — 手动注册 RabbitMQConfig Bean，避免触发完整的自动配置链路
     */
    @Configuration
    static class MqTestConfig {
        @Bean
        public RabbitMQConfig rabbitMQConfig() {
            return new RabbitMQConfig();
        }
    }

    @Autowired(required = false)
    private RabbitMQConfig rabbitMQConfig;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // ==================== MqMessage 单元测试 ====================

    @Test
    void testMqMessageCreation() {
        MqMessage msg = new MqMessage("mail.send", "{\"to\":\"test@example.com\"}");
        assertNotNull(msg.getTimestamp());
        assertEquals("mail.send", msg.getEventType());
        assertEquals("{\"to\":\"test@example.com\"}", msg.getData());
    }

    @Test
    void testMqMessageTraceIdAutoGenerate() {
        MqMessage msg = new MqMessage("audit.log", "{}");
        // traceId 由 MessageSender 生成，此处为 null
        assertNull(msg.getTraceId());
    }

    @Test
    void testMqMessageJsonSerialization() throws Exception {
        MqMessage msg = new MqMessage("test.event", "{\"key\":\"value\"}");
        msg.setTraceId("abc123");

        String json = objectMapper.writeValueAsString(msg);
        MqMessage restored = objectMapper.readValue(json, MqMessage.class);

        assertEquals("test.event", restored.getEventType());
        assertEquals("{\"key\":\"value\"}", restored.getData());
        assertEquals("abc123", restored.getTraceId());
    }

    @Test
    void testMqMessageJsonRoundTripWithMailPayload() throws Exception {
        Map<String, String> payload = Map.of("to", "user@test.com", "code", "123456", "mode", "verifyCode");
        String jsonData = objectMapper.writeValueAsString(payload);

        MqMessage msg = new MqMessage("mail.send", jsonData);
        String json = objectMapper.writeValueAsString(msg);

        MqMessage restored = objectMapper.readValue(json, MqMessage.class);
        @SuppressWarnings("unchecked")
        Map<String, String> restoredPayload = objectMapper.readValue(restored.getData(), Map.class);

        assertEquals("user@test.com", restoredPayload.get("to"));
        assertEquals("123456", restoredPayload.get("code"));
        assertEquals("verifyCode", restoredPayload.get("mode"));
    }

    // ==================== RabbitMQConfig Bean 验证 ====================

    @Test
    void testMailExchangeBean() {
        DirectExchange exchange = rabbitMQConfig.mailExchange();
        assertEquals("mes.mail.exchange", exchange.getName());
        assertTrue(exchange.isDurable());
    }

    @Test
    void testEventExchangeBean() {
        TopicExchange exchange = rabbitMQConfig.eventExchange();
        assertEquals("mes.event.exchange", exchange.getName());
        assertTrue(exchange.isDurable());
    }

    @Test
    void testAuditExchangeBean() {
        DirectExchange exchange = rabbitMQConfig.auditExchange();
        assertEquals("mes.audit.exchange", exchange.getName());
        assertTrue(exchange.isDurable());
    }

    @Test
    void testMailQueueBean() {
        Queue queue = rabbitMQConfig.mailQueue();
        assertEquals("mes.mail.queue", queue.getName());
        assertTrue(queue.isDurable());
        // 验证 DLQ 配置
        assertEquals("mes.mail.dlq", queue.getArguments().get("x-dead-letter-routing-key"));
        assertEquals("", queue.getArguments().get("x-dead-letter-exchange"));
    }

    @Test
    void testMailDLQBean() {
        Queue dlq = rabbitMQConfig.mailDeadLetterQueue();
        assertEquals("mes.mail.dlq", dlq.getName());
        assertTrue(dlq.isDurable());
    }

    @Test
    void testAuditQueueBean() {
        Queue queue = rabbitMQConfig.auditQueue();
        assertEquals("mes.audit.queue", queue.getName());
        assertTrue(queue.isDurable());
        assertEquals("mes.audit.dlq", queue.getArguments().get("x-dead-letter-routing-key"));
    }

    @Test
    void testAuditDLQBean() {
        Queue dlq = rabbitMQConfig.auditDeadLetterQueue();
        assertEquals("mes.audit.dlq", dlq.getName());
        assertTrue(dlq.isDurable());
    }

    @Test
    void testMessageConverterBean() {
        MessageConverter converter = rabbitMQConfig.messageConverter();
        assertNotNull(converter);
        assertTrue(converter instanceof Jackson2JsonMessageConverter);
    }

    @Test
    void testMailBinding() {
        Binding binding = rabbitMQConfig.mailBinding();
        assertEquals("mes.mail.queue", binding.getDestination());
        assertEquals("mes.mail.exchange", binding.getExchange());
        assertEquals("mail.send", binding.getRoutingKey());
    }

    @Test
    void testAuditBinding() {
        Binding binding = rabbitMQConfig.auditBinding();
        assertEquals("mes.audit.queue", binding.getDestination());
        assertEquals("mes.audit.exchange", binding.getExchange());
        assertEquals("audit.log", binding.getRoutingKey());
    }

    // ==================== 消息拓扑完整性验证 ====================

    @Test
    void testCompleteTopologyConsistency() {
        // 所有队列名称唯一
        assertNotEquals(rabbitMQConfig.mailQueue().getName(), rabbitMQConfig.auditQueue().getName());
        assertNotEquals(rabbitMQConfig.workOrderQueue().getName(), rabbitMQConfig.mailQueue().getName());
        assertNotEquals(rabbitMQConfig.workOrderQueue().getName(), rabbitMQConfig.auditQueue().getName());

        // 所有交换机名称唯一
        assertNotEquals(rabbitMQConfig.mailExchange().getName(), rabbitMQConfig.eventExchange().getName());
        assertNotEquals(rabbitMQConfig.mailExchange().getName(), rabbitMQConfig.auditExchange().getName());

        // DLQ 与主队列不重名
        assertNotEquals(rabbitMQConfig.mailQueue().getName(), rabbitMQConfig.mailDeadLetterQueue().getName());
        assertNotEquals(rabbitMQConfig.auditQueue().getName(), rabbitMQConfig.auditDeadLetterQueue().getName());
    }

    // ==================== 消息 payload 场景覆盖 ====================

    @Test
    void testAuditPayloadSerialization() throws Exception {
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("operatorId", 1L);
        payload.put("operation", "LOGIN");
        payload.put("targetType", "SysUser");
        payload.put("targetId", 1L);
        payload.put("detail", "{\"result\":\"success\"}");
        payload.put("ip", "127.0.0.1");

        String jsonData = objectMapper.writeValueAsString(payload);
        MqMessage msg = new MqMessage("audit.log", jsonData);

        String json = objectMapper.writeValueAsString(msg);
        MqMessage restored = objectMapper.readValue(json, MqMessage.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> restoredPayload = objectMapper.readValue(restored.getData(), Map.class);

        assertEquals(1, ((Number) restoredPayload.get("operatorId")).intValue());
        assertEquals("LOGIN", restoredPayload.get("operation"));
        assertEquals("127.0.0.1", restoredPayload.get("ip"));
    }

    @Test
    void testWorkOrderEventPayloadSerialization() throws Exception {
        Map<String, Object> payload = Map.of(
                "orderId", 100L,
                "orderNo", "WO202607150001",
                "productName", "测试产品",
                "status", 3
        );

        String jsonData = objectMapper.writeValueAsString(payload);
        MqMessage msg = new MqMessage("notify.workorder.3", jsonData);

        String json = objectMapper.writeValueAsString(msg);
        MqMessage restored = objectMapper.readValue(json, MqMessage.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> restoredPayload = objectMapper.readValue(restored.getData(), Map.class);

        assertEquals(100, ((Number) restoredPayload.get("orderId")).intValue());
        assertEquals("WO202607150001", restoredPayload.get("orderNo"));
        assertEquals("测试产品", restoredPayload.get("productName"));
        assertEquals(3, ((Number) restoredPayload.get("status")).intValue());
    }
}
