package com.itheima.mes1.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Token 认证拦截器：从 Header 取 token，查 Redis 获取 userId 和权限集合
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.replace("Bearer ", "");
        }
        // SSE/EventSource 不支持自定义 Header，允许通过 query param 传 token
        if (token == null) {
            token = request.getParameter("token");
        }
        if (token == null || token.isEmpty()) {
            writeJson(response, 401, "未登录或 Token 格式错误");
            return false;
        }
        Object userIdObj = redisTemplate.opsForValue().get("token:" + token);
        if (userIdObj == null) {
            writeJson(response, 401, "Token 已过期，请重新登录");
            return false;
        }
        Long userId = ((Number) userIdObj).longValue();

        // 获取权限集合（JSON 反序列化为 List，需转换）
        Object permObj = redisTemplate.opsForValue().get("perm:" + userId);
        Set<String> permissions;
        if (permObj instanceof Set<?> s) {
            @SuppressWarnings("unchecked")
            Set<String> set = (Set<String>) s;
            permissions = set;
        } else if (permObj instanceof List<?> list) {
            permissions = new java.util.HashSet<>();
            for (Object item : list) {
                if (item instanceof String s) permissions.add(s);
            }
        } else {
            permissions = Set.of();
        }

        // 存入 request 供后续使用
        request.setAttribute("userId", userId);
        request.setAttribute("permissions", permissions);

        // 刷新 token TTL
        redisTemplate.expire("token:" + token, 24, TimeUnit.HOURS);
        redisTemplate.expire("perm:" + userId, 24, TimeUnit.HOURS);

        return true;
    }

    private void writeJson(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(mapper.writeValueAsString(Map.of("code", status, "message", message)));
    }
}
