package com.itheima.mes1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Mes1Application {

    public static void main(String[] args) {
        SpringApplication.run(Mes1Application.class, args);
    }

    /** 放宽 Tomcat 请求头校验，避免部分客户端的合法请求被拒绝 */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            connector.setProperty("relaxedPathChars", "|{}[],^");
            connector.setProperty("relaxedQueryChars", "|{}[],^");
        });
    }
}
