package com.itheima.mes1.config;

import com.itheima.mes1.common.interceptor.AuthInterceptor;
import com.itheima.mes1.common.interceptor.PermissionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;
    @Autowired
    private PermissionInterceptor permissionInterceptor;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Token 认证拦截器
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/captcha/**",
                        "/api/register/**",
                        "/api/password/**",
                        "/api/public/**",
                        "/api/portal/login",
                        "/api/portal/register",
                        "/api/portal/products/**",
                        "/api/portal/categories",
                        "/doc.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                );

        // 权限校验拦截器（portal 不使用 RBAC，全部排除）
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/captcha/**",
                        "/api/register/**",
                        "/api/user/profile",
                        "/api/user/password",
                        "/api/public/**",
                        "/api/portal/**",
                        "/doc.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                );
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // SPA fallback — 非 API / 非静态资源的请求统一转发到 index.html
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}
