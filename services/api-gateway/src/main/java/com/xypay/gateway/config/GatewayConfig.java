package com.xypay.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Customer Service routes
                .route("customer-service", r -> r
                        .path("/api/customers/**")
                        .uri("lb://customer-service"))
                
                // Account Service routes
                .route("account-service", r -> r
                        .path("/api/accounts/**")
                        .uri("lb://account-service"))
                
                // Transaction Service routes
                .route("transaction-service", r -> r
                        .path("/api/transactions/**")
                        .uri("lb://transaction-service"))
                
                // Notification Service routes
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri("lb://notification-service"))
                
                // Treasury Service routes
                .route("treasury-service", r -> r
                        .path("/api/treasury/**")
                        .uri("lb://treasury-service"))
                
                // Analytics Service routes
                .route("analytics-service", r -> r
                        .path("/api/analytics/**")
                        .uri("lb://analytics-service"))
                
                // Web UI Service routes (fallback for all other requests)
                .route("web-ui-service", r -> r
                        .path("/**")
                        .uri("lb://web-ui-service"))
                
                .build();
    }
}
