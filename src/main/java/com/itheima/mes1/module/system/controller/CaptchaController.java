package com.itheima.mes1.module.system.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Tag(name = "验证码")
@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Operation(summary = "获取图形验证码（注册用）")
    @GetMapping("/image")
    public Map<String, Object> image() {
        // 生成线段干扰图形验证码：宽200 高80 4位字符
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(200, 80, 4, 20);
        String code = captcha.getCode();
        log.info("图形验证码: code={}", code);

        // 存入 Redis，3 分钟有效
        String key = "captcha:image:" + UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(key, code, 3, TimeUnit.MINUTES);

        Map<String, Object> result = new HashMap<>();
        result.put("captchaKey", key);
        result.put("image", captcha.getImageBase64Data());
        return result;
    }

    @Operation(summary = "获取数字运算验证码（登录用）")
    @GetMapping("/math")
    public Map<String, Object> math() {
        // 随机生成 1-20 之间的加减乘
        int a = (int) (Math.random() * 20) + 1;
        int b = (int) (Math.random() * 20) + 1;
        int op = (int) (Math.random() * 3); // 0:+ 1:- 2:*
        String question;
        int answer;
        switch (op) {
            case 0 -> { question = a + " + " + b + " = ?"; answer = a + b; }
            case 1 -> {
                if (a < b) { int t = a; a = b; b = t; }
                question = a + " - " + b + " = ?"; answer = a - b;
            }
            default -> { question = a + " × " + b + " = ?"; answer = a * b; }
        }

        // 存入 Redis，3 分钟有效
        String key = "captcha:math:" + UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(key, String.valueOf(answer), 3, TimeUnit.MINUTES);
        log.info("数学验证码: question={}, answer={}", question, answer);

        Map<String, Object> result = new HashMap<>();
        result.put("captchaKey", key);
        result.put("question", question);
        return result;
    }
}
