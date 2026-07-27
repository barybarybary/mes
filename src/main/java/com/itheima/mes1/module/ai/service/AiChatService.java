package com.itheima.mes1.module.ai.service;

import cn.hutool.json.JSONUtil;
import com.itheima.mes1.module.ai.entity.AiConversation;
import com.itheima.mes1.module.ai.entity.AiMessage;
import com.itheima.mes1.module.ai.mapper.AiConversationMapper;
import com.itheima.mes1.module.ai.mapper.AiMessageMapper;
import com.itheima.mes1.module.knowledge.entity.KbChunk;
import com.itheima.mes1.module.knowledge.mapper.KbChunkMapper;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    private volatile AiToolExecutor toolExecutor;
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
        List<ToolSpecification> specs = ToolSpecifications.toolSpecificationsFrom(toolService);
        toolExecutor = new AiToolExecutor(toolService, chatModel, specs);
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
                2. 回答必须简洁：用2-5句话概括核心数据即可，禁止使用 Markdown 表格、禁止使用大标题、禁止长篇分段报告
                3. 用户问"有哪些订单"或搜索订单时，调用 searchOrders 或 listSaleOrders
                4. 用户问具体订单号时，调用 getSaleOrder 查详情
                5. 用户问库存/有没有货时，调用 queryInventory
                6. 用户问工单/生产进度时，调用 listWorkOrders 或 getWorkOrder
                7. 用户问产品信息时，调用 queryProduct
                8. 用户问客户时，调用 listCustomers
                9. 用户问系统概况/今天怎么样时，调用 getDashboardSummary
                10. 用户要求"监督"、"巡检"、"系统健康检查"时，调用 systemHealthCheck 进行全面诊断
                11. 不要把工具名告诉用户，直接用自然语言回答""";

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
                answer = toolExecutor.callWithTools(chatMessages);
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