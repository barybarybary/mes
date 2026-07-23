package com.itheima.mes1.module.ai.service;

import cn.hutool.json.JSONUtil;
import com.itheima.mes1.module.ai.entity.AiConversation;
import com.itheima.mes1.module.ai.entity.AiMessage;
import com.itheima.mes1.module.ai.mapper.AiConversationMapper;
import com.itheima.mes1.module.ai.mapper.AiMessageMapper;
import com.itheima.mes1.module.knowledge.entity.KbChunk;
import com.itheima.mes1.module.knowledge.mapper.KbChunkMapper;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiChatService {

    @Autowired private AiConversationMapper conversationMapper;
    @Autowired private AiMessageMapper messageMapper;
    @Autowired private KbChunkMapper chunkMapper;
    @Autowired private AiToolService toolService;

    @Value("${langchain4j.openai.api-key}")
    private String apiKey;
    @Value("${langchain4j.openai.model-name:deepseek-chat}")
    private String modelName;
    @Value("${langchain4j.openai.base-url:https://api.deepseek.com}")
    private String baseUrl;

    private volatile OpenAiChatModel chatModel;
    private volatile List<ToolSpecification> toolSpecifications;
    private volatile boolean initAttempted;

    /** 懒加载 AI 模型，避免启动时因缺少 API Key 而崩溃 */
    private synchronized void ensureInitialized() {
        if (initAttempted) return;
        initAttempted = true;
        if (apiKey == null || apiKey.isBlank()) {
            return; // API Key 未配置，不初始化
        }
        chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .temperature(0.7)
                .build();
        toolSpecifications = ToolSpecifications.toolSpecificationsFrom(toolService);
    }

    public List<AiConversation> listConversations(Long userId) {
        return conversationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiConversation>()
                        .eq(AiConversation::getUserId, userId)
                        .orderByDesc(AiConversation::getUpdateTime));
    }

    public List<AiMessage> getMessages(Long conversationId) {
        return messageMapper.selectByConversationId(conversationId);
    }

    public AiMessage chat(Long userId, Long conversationId, String question) {
        // 创建或获取会话
        AiConversation conv;
        if (conversationId == null) {
            conv = new AiConversation();
            String title = question.length() > 30 ? question.substring(0, 30) + "..." : question;
            conv.setTitle(title);
            conv.setUserId(userId);
            conv.setModel(modelName);
            conv.setMessageCount(0);
            conversationMapper.insert(conv);
        } else {
            conv = conversationMapper.selectById(conversationId);
        }

        // 保存用户消息
        AiMessage userMsg = new AiMessage();
        userMsg.setConversationId(conv.getId());
        userMsg.setRole("user");
        userMsg.setContent(question);
        userMsg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(userMsg);

        // RAG 检索相关知识
        List<KbChunk> relevantChunks = searchRelevantChunks(question);
        String context = relevantChunks.stream()
                .map(KbChunk::getContent)
                .collect(Collectors.joining("\n\n"));

        // 构建系统提示
        String systemPrompt = """
                你是「造易MES系统」的AI助手，能直接查询系统内的真实数据来回答用户问题。

                重要规则:
                1. 当用户询问订单、库存、工单、产品、客户、系统概况等具体数据时，必须使用工具(Tool)查询，不要凭猜测回答
                2. 用友好专业的中文回答，将查询结果整理成易读的格式
                3. 用户问"有哪些订单"或搜索订单时，调用 searchOrders 或 listSaleOrders
                4. 用户问具体订单号时，调用 getSaleOrder 查详情
                5. 用户问库存/有没有货时，调用 queryInventory
                6. 用户问工单/生产进度时，调用 listWorkOrders 或 getWorkOrder
                7. 用户问产品信息时，调用 queryProduct
                8. 用户问客户时，调用 listCustomers
                9. 用户问系统概况/今天怎么样时，调用 getDashboardSummary
                10. 用户要求"监督"、"巡检"、"系统健康检查"时，调用 systemHealthCheck 进行全面诊断
                11. 用户要求生成报表、导出数据、制作统计报告时，调用 generateReport 工具。reportType: sales(销售报表)/production(生产报表)/inventory(库存报表)/summary(综合报表); timeRange: 本周/本月/上月/近7天/近30天
                12. 用户询问历史报表或"我的报表"时，调用 getMyReports 工具
                13. 不要把工具名告诉用户，直接用自然语言回答""";

        if (!context.isEmpty()) {
            systemPrompt += "\n\n以下参考资料仅用于回答知识性问题（数据查询优先用工具）:\n" + context;
        }

        // 获取历史消息
        List<AiMessage> history = messageMapper.selectByConversationId(conv.getId());
        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(SystemMessage.from(systemPrompt));

        List<AiMessage> recentHistory = history.size() > 11
                ? history.subList(history.size() - 11, history.size() - 1)
                : history.subList(0, Math.max(0, history.size() - 1));
        for (AiMessage msg : recentHistory) {
            if ("user".equals(msg.getRole())) {
                chatMessages.add(UserMessage.from(msg.getContent()));
            } else {
                chatMessages.add(dev.langchain4j.data.message.AiMessage.from(msg.getContent()));
            }
        }
        chatMessages.add(UserMessage.from(question));

        // 调用 AI 模型（带工具）
        String answer;
        List<String> sourcesList = new ArrayList<>();

        ensureInitialized();
        if (chatModel == null) {
            answer = "AI 服务未配置，请设置 DEEPSEEK_API_KEY 环境变量后重启。";
        } else {
            try {
                answer = callWithTools(chatMessages, sourcesList);
            } catch (Exception e) {
                answer = "AI 服务暂时不可用: " + e.getMessage();
            }
        }

        // 保存助手消息
        AiMessage aiMsg = new AiMessage();
        aiMsg.setConversationId(conv.getId());
        aiMsg.setRole("assistant");
        aiMsg.setContent(answer);

        if (!relevantChunks.isEmpty()) {
            aiMsg.setSources(JSONUtil.toJsonStr(relevantChunks.stream()
                    .map(c -> Map.of("documentId", c.getDocumentId(), "chunkIndex", c.getChunkIndex()))
                    .toList()));
        }
        aiMsg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(aiMsg);

        conv.setMessageCount(conv.getMessageCount() != null ? conv.getMessageCount() + 2 : 2);
        conv.setUpdateTime(LocalDateTime.now());
        conversationMapper.updateById(conv);

        return aiMsg;
    }

    /** 带 Tool Calling 的对话循环 */
    private String callWithTools(List<ChatMessage> messages, List<String> sources) {
        int maxRounds = 3; // 最多3轮工具调用
        for (int round = 0; round < maxRounds; round++) {
            Response<dev.langchain4j.data.message.AiMessage> response = chatModel.generate(messages, toolSpecifications);
            dev.langchain4j.data.message.AiMessage aiMessage = response.content();

            // 检查是否有工具调用请求
            if (!aiMessage.hasToolExecutionRequests()) {
                return aiMessage.text();
            }

            // 执行工具调用
            messages.add(aiMessage);
            for (ToolExecutionRequest req : aiMessage.toolExecutionRequests()) {
                String toolResult = executeTool(req.name(), req.arguments());
                sources.add("工具:" + req.name());
                messages.add(ToolExecutionResultMessage.from(req, toolResult));
            }
        }
        // 超过最大轮次，最后一次不带工具调用
        Response<dev.langchain4j.data.message.AiMessage> finalResp = chatModel.generate(messages);
        return finalResp.content().text();
    }

    /** 执行工具调用（反射匹配 @Tool 方法） */
    private String executeTool(String toolName, String argumentsJson) {
        try {
            Method[] methods = toolService.getClass().getMethods();
            for (Method m : methods) {
                if (m.getName().equals(toolName)) {
                    // 解析参数
                    Map<String, Object> args = argumentsJson != null && !argumentsJson.isEmpty()
                            ? JSONUtil.parseObj(argumentsJson)
                            : Map.of();
                    Object[] params = new Object[m.getParameterCount()];
                    java.lang.reflect.Parameter[] javaParams = m.getParameters();
                    for (int i = 0; i < javaParams.length; i++) {
                        Object value = args.get(javaParams[i].getName());
                        if (value != null) {
                            // 类型转换
                            Class<?> type = javaParams[i].getType();
                            if (type == Integer.class && value instanceof Number n) {
                                params[i] = n.intValue();
                            } else {
                                params[i] = value.toString();
                            }
                        } else {
                            params[i] = null;
                        }
                    }
                    return (String) m.invoke(toolService, params);
                }
            }
            return "工具 " + toolName + " 不存在";
        } catch (Exception e) {
            return "工具调用失败: " + e.getMessage();
        }
    }

    private List<KbChunk> searchRelevantChunks(String query) {
        List<KbChunk> allChunks = chunkMapper.selectList(null);
        String[] keywords = query.replaceAll("[，。！？\\s]+", " ").split("[\\s]+");
        return allChunks.stream()
                .filter(chunk -> {
                    String content = chunk.getContent().toLowerCase();
                    for (String kw : keywords) {
                        if (kw.length() >= 2 && content.contains(kw.toLowerCase())) {
                            return true;
                        }
                    }
                    return false;
                })
                .limit(5)
                .collect(Collectors.toList());
    }
}