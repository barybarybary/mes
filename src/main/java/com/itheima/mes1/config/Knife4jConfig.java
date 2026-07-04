package com.itheima.mes1.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("造易 ZaoYi API")
                        .version("1.0")
                        .description("销售 → 库存 → 生产 全链路管理")
                        .contact(new Contact().name("dev")));
    }
}
