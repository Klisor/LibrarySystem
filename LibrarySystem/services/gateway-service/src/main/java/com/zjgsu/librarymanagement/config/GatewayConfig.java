package com.zjgsu.librarymanagement.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;

@Configuration
public class GatewayConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);  // 允许凭证

        // ❌ 错误：不能同时使用 allowCredentials=true 和 allowedOrigins="*"
        // config.addAllowedOrigin("*");

        // ✅ 正确：使用 allowedOriginPatterns 代替
        config.addAllowedOriginPattern("*");  // 使用 allowedOriginPatterns

        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Arrays.asList(
                "Content-Type",
                "Authorization",
                "X-Requested-With",
                "X-User-Id",
                "X-Username",
                "X-User-Role",
                "Accept",
                "Cache-Control",
                "Pragma"
        ));
        config.setExposedHeaders(Arrays.asList(
                "Content-Disposition",
                "X-User-Id",
                "X-Username",
                "X-User-Role"
        ));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}