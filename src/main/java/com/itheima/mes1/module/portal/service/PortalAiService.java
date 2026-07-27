package com.itheima.mes1.module.portal.service;

import com.itheima.mes1.module.ai.service.AiToolExecutor;
import com.itheima.mes1.module.ai.service.AiToolService;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 客户门户 AI 客服 — 轻量版，面向外部客户
 * 无历史记录持久化（无状态），仅回答产品/订单/库存相关问题
 */
@Service
public class PortalAiService {

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

    private synchronized void ensureInitialized() {
        if (initAttempted) return;
        initAttempted = true;
        if (apiKey == null || apiKey.isBlank()) return;
        chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey).modelName(modelName).baseUrl(baseUrl)
                .temperature(0.5)
                .build();
        List<ToolSpecification> specs = ToolSpecifications.toolSpecificationsFrom(toolService);
        toolExecutor = new AiToolExecutor(toolService, chatModel, specs);
    }

    /**
     * 客户提问 → AI 回复（无状态，不存储历史）
     */
    public String chat(String question) {
        if (question == null || question.isBlank()) return "请输入您的问题。";

        ensureInitialized();
        if (chatModel == null) {
            return "AI 客服暂未上线，请联系管理员。";
        }

        String systemPrompt = """
                你是「造易商城」的智能客服，专门为外部客户提供服务。
                你可以通过工具查询系统内的真实数据来回答客户的问题。

                重要规则：
                1. 当客户询问产品信息时，使用 queryProduct 工具查询
                2. 当客户询问库存情况时，使用 queryInventory 工具查询
                3. 当客户询问订单时，使用 getSaleOrder 工具查询（需要订单号）
                4. 当客户询问有哪些产品/分类时，使用 listProducts 工具查询
                5. 用友好、专业、热情的中文回答，将查询结果整理成易读的格式
                6. 如果客户问工时、生产进度、报表等后台管理问题，礼貌告知这是内部管理功能，建议联系客服
                7. 不要说"工具"、"函数"等技术术语，直接用自然语言回答
                8. 产品推荐时突出卖点和性价比
                9. 订单查询需要客户提供订单号""";

        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(SystemMessage.from(systemPrompt));
            messages.add(UserMessage.from(question));

            return toolExecutor.callWithTools(messages);
        } catch (Exception e) {
            return "抱歉，AI 客服暂时无法响应：" + e.getMessage();
        }
    }
}
