package com.zjgsu.librarymanagement.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FeignConfig {

    @Value("${app.jwt.service-token:}")
    private String serviceToken;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                log.info("Feign请求拦截器执行 - 方法: {}, URL: {}",
                        template.method(), template.url());
                log.info("配置的serviceToken: {}", serviceToken);

                // 添加 Authorization 头
                if (serviceToken != null && !serviceToken.isEmpty()) {
                    template.header("Authorization", serviceToken);
                    log.info("已添加Authorization头: {}", serviceToken.substring(0, Math.min(50, serviceToken.length())) + "...");
                } else {
                    log.error("未配置serviceToken，Feign调用将无法认证！");
                }

                // 添加 Content-Type 头
                template.header("Content-Type", "application/json");
            }
        };
    }
}