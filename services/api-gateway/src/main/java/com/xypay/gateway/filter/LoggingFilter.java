package com.xypay.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestId = java.util.UUID.randomUUID().toString();
        
        // Add request ID to headers
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Request-ID", requestId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        long startTime = System.currentTimeMillis();
        
        return chain.filter(mutatedExchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            ServerHttpResponse response = mutatedExchange.getResponse();
            
            logRequest(mutatedRequest, response, requestId, duration);
        }));
    }

    private void logRequest(ServerHttpRequest request, ServerHttpResponse response, String requestId, long duration) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String status = response.getStatusCode() != null ? response.getStatusCode().toString() : "UNKNOWN";
        String userAgent = request.getHeaders().getFirst("User-Agent");
        String clientIp = getClientIp(request);
        
        System.out.printf("[%s] [%s] %s %s -> %s (%dms) [%s] [%s]%n",
                timestamp, requestId, method, path, status, duration, clientIp, userAgent);
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null ? 
            request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    @Override
    public int getOrder() {
        return 1; // After authentication filter
    }
}
