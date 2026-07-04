package com.itheima.mes1.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.mes1.common.annotation.RequirePermission;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.Set;

/**
 * 权限校验拦截器：检查方法上的 @RequirePermission 注解
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        RequirePermission annotation = hm.getMethodAnnotation(RequirePermission.class);
        if (annotation == null) {
            return true; // 无注解 = 公开接口
        }

        @SuppressWarnings("unchecked")
        Set<String> permissions = (Set<String>) request.getAttribute("permissions");
        if (permissions == null) {
            permissions = Set.of();
        }

        // 管理员跳过权限检查
        if (permissions.contains("admin") || permissions.contains("*")) {
            return true;
        }

        String required = annotation.value();
        if (permissions.contains(required)) {
            return true;
        }

        writeJson(response, 403, "权限不足: " + required);
        return false;
    }

    private void writeJson(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(mapper.writeValueAsString(Map.of("code", status, "message", message)));
    }
}
