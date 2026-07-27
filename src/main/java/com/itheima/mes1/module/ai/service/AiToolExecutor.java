package com.itheima.mes1.module.ai.service;

import cn.hutool.json.JSONUtil;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;

import java.lang.reflect.Method;
import java.util.*;

/**
 * AI 工具调用执行器 — 公共逻辑，供 AiChatService 和 PortalAiService 共用
 */
public class AiToolExecutor {

    private final AiToolService toolService;
    private final OpenAiChatModel chatModel;
    private final List<ToolSpecification> toolSpecifications;

    public AiToolExecutor(AiToolService toolService, OpenAiChatModel chatModel,
                          List<ToolSpecification> toolSpecifications) {
        this.toolService = toolService;
        this.chatModel = chatModel;
        this.toolSpecifications = toolSpecifications;
    }

    /**
     * 带 Tool Calling 的对话循环，最多 3 轮工具调用
     */
    public String callWithTools(List<ChatMessage> messages) {
        int maxRounds = 3;
        for (int round = 0; round < maxRounds; round++) {
            Response<dev.langchain4j.data.message.AiMessage> response =
                    chatModel.generate(messages, toolSpecifications);
            dev.langchain4j.data.message.AiMessage aiMessage = response.content();

            if (!aiMessage.hasToolExecutionRequests()) {
                return aiMessage.text();
            }

            messages.add(aiMessage);
            for (ToolExecutionRequest req : aiMessage.toolExecutionRequests()) {
                String toolResult = executeTool(req.name(), req.arguments());
                messages.add(ToolExecutionResultMessage.from(req, toolResult));
            }
        }
        Response<dev.langchain4j.data.message.AiMessage> finalResp = chatModel.generate(messages);
        return finalResp.content().text();
    }

    /** 执行工具调用（反射匹配 @Tool 方法） */
    private String executeTool(String toolName, String argumentsJson) {
        try {
            Method[] methods = toolService.getClass().getMethods();
            for (Method m : methods) {
                if (m.getName().equals(toolName)) {
                    Map<String, Object> args = argumentsJson != null && !argumentsJson.isEmpty()
                            ? JSONUtil.parseObj(argumentsJson) : Map.of();
                    Object[] params = new Object[m.getParameterCount()];
                    java.lang.reflect.Parameter[] javaParams = m.getParameters();
                    for (int i = 0; i < javaParams.length; i++) {
                        Object value = args.get(javaParams[i].getName());
                        if (value != null) {
                            Class<?> type = javaParams[i].getType();
                            if ((type == Integer.class || type == int.class) && value instanceof Number n) {
                                params[i] = n.intValue();
                            } else {
                                params[i] = value.toString();
                            }
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
}
