package com.itheima.mes1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 生产环境应通过 CORS_ALLOWED_ORIGINS 环境变量指定具体域名，如 http://example.com,https://admin.example.com
        config.setAllowedOriginPatterns(CorsUtil.parseOrigins());
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    /**
     * 解析 CORS 允许的来源，默认仅允许本地开发环境。
     * 生产环境设置 CORS_ALLOWED_ORIGINS=你的域名（逗号分隔多个）。
     */
    static class CorsUtil {
        static List<String> parseOrigins() {
            String origins = System.getenv("CORS_ALLOWED_ORIGINS");
            if (origins == null || origins.isBlank()) {
                // 默认仅允许本地开发
                return List.of("http://localhost:8080", "http://localhost:8081", "http://localhost:8082", "http://127.0.0.1:8080", "http://127.0.0.1:8082");
            }
            return List.of(origins.split(","));
        }
    }
}
