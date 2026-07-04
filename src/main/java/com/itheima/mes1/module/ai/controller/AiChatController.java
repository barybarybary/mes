package com.itheima.mes1.module.ai.controller;

import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.ai.entity.AiConversation;
import com.itheima.mes1.module.ai.entity.AiMessage;
import com.itheima.mes1.module.ai.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "AI助手")
@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    @RequirePermission("ai:chat:list")
    @Operation(summary = "会话列表")
    @GetMapping("/conversations")
    public Result<List<AiConversation>> conversations(@RequestParam Long userId) {
        return Result.ok(aiChatService.listConversations(userId));
    }

    @RequirePermission("ai:chat:list")
    @Operation(summary = "对话消息")
    @GetMapping("/conversations/{id}/messages")
    public Result<List<AiMessage>> messages(@PathVariable Long id) {
        return Result.ok(aiChatService.getMessages(id));
    }

    @RequirePermission("ai:chat:list")
    @Operation(summary = "发送消息 (RAG对话)")
    @PostMapping("/chat")
    public Result<AiMessage> chat(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Long conversationId = body.get("conversationId") != null
                ? Long.valueOf(body.get("conversationId").toString()) : null;
        String question = (String) body.get("question");

        AiMessage reply = aiChatService.chat(userId, conversationId, question);
        return Result.ok(reply);
    }

}
