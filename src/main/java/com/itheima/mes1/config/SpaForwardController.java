package com.itheima.mes1.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SPA 兜底：将非 API、非静态资源的请求转发到 index.html，由 Vue Router 接管。
 * 解决直接访问 /login 或刷新页面时 Spring Boot 返回 404 的问题。
 */
@Controller
public class SpaForwardController {

    /**
     * 匹配无后缀的路径（非静态资源），但排除 API 请求
     */
    @RequestMapping(value = "/{path:[^.]*}")
    public String forwardNonApi(@org.springframework.web.bind.annotation.PathVariable String path) {
        if (path.startsWith("api")) return null;
        return "forward:/index.html";
    }

    /**
     * 匹配嵌套路径，如 /base/product
     */
    @RequestMapping(value = "/{path1:[^.]*}/{path2:[^.]*}")
    public String forwardNested(@org.springframework.web.bind.annotation.PathVariable String path1) {
        if (path1.startsWith("api")) return null;
        return "forward:/index.html";
    }

    /**
     * 匹配深层嵌套路径，如 /portal/products/123
     */
    @RequestMapping(value = "/{path1:[^.]*}/{path2:[^.]*}/{path3:[^.]*}")
    public String forwardDeep(@org.springframework.web.bind.annotation.PathVariable String path1) {
        if (path1.startsWith("api")) return null;
        return "forward:/index.html";
    }
}
