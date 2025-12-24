package com.zjgsu.librarymanagement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final com.zjgsu.librarymanagement.filter.JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                // ======== 白名单 ========
                .excludePathPatterns(
                        "/api/users/register",
                        "/api/users",
                        "/users",
                        "/api/auth/login",
                        "/users/register",
                        "/auth/login",            // 登录
                        "/auth/register",         // 注册
                        "/swagger-ui/**",         // swagger
                        "/error",                 // Spring Boot 默认错误页
                        "/static/**",             // 静态资源
                        "/public/**"
                )
                .addPathPatterns("/**");        // 其余全部拦截
    }
}