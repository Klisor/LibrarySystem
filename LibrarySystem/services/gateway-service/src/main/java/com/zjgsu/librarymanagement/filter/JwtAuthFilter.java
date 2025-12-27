package com.zjgsu.librarymanagement.filter;

import com.zjgsu.librarymanagement.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    // 白名单路径
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/auth/login",
            "/api/users/register",
            "/api/actuator/health",
            "/actuator/health"
    );

    // 不需要JWT验证的路径谓词
    private final Predicate<ServerHttpRequest> isSecured = request ->
            WHITE_LIST.stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            String method = request.getMethod().name();

            log.info("网关请求: {} {}", method, path);

            // 1. 检查是否为白名单路径
            if (!isSecured.test(request)) {
                log.debug("白名单路径: {}", path);

                // ✅ 白名单也要添加网关标识
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-Gateway-Request", "true")  // 添加网关标识
                        .header("X-Gateway-Source", "gateway-service")  // 可以添加更多标识
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            }

            // 2. 获取Authorization头
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("请求缺少Authorization头: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Authorization头格式错误: {}", authHeader);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);

            // 3. 验证Token
            try {
                if (!jwtUtil.validateToken(token)) {
                    log.warn("Token验证失败: {}", path);
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                // 4. 提取用户信息
                String username = jwtUtil.extractUsername(token);
                Long userId = jwtUtil.extractUserId(token);
                String role = jwtUtil.extractRole(token);

                log.info("JWT验证通过 - 用户: {} (ID: {}, 角色: {})", username, userId, role);

                // 5. 添加用户信息到请求头，传递给下游服务
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-Username", username)
                        .header("X-User-Role", role)
                        .header("X-Gateway-Request", "true")  // 添加网关标识
                        .header("X-Gateway-Source", "gateway-service")
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                log.error("JWT处理异常: {}", e.getMessage(), e);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }
    public static class Config {
        // 配置属性
    }
}