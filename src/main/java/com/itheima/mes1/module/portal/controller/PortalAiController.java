package com.itheima.mes1.module.portal.controller;

import com.itheima.mes1.common.Result;
import com.itheima.mes1.module.portal.service.PortalAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "客户门户AI客服")
@RestController
@RequestMapping("/api/portal/ai")
public class PortalAiController {

    @Autowired
    private PortalAiService portalAiService;

    @Operation(summary = "AI客服对话（无状态）")
    @PostMapping("/chat")
    public Result<Map<String, String>> chat(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        String answer = portalAiService.chat(question);
        return Result.ok(Map.of("content", answer));
    }
}
