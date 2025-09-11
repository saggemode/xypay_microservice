package com.xypay.xypay.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Service
public class CircuitBreakerService {
    
    private final CircuitBreaker customerServiceCircuitBreaker;
    private final CircuitBreaker accountServiceCircuitBreaker;
    private final CircuitBreaker transactionServiceCircuitBreaker;
    private final CircuitBreaker notificationServiceCircuitBreaker;
    private final CircuitBreaker treasuryServiceCircuitBreaker;
    private final CircuitBreaker analyticsServiceCircuitBreaker;
    
    public CircuitBreakerService() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .build();
        
        this.customerServiceCircuitBreaker = CircuitBreaker.of("customer-service", config);
        this.accountServiceCircuitBreaker = CircuitBreaker.of("account-service", config);
        this.transactionServiceCircuitBreaker = CircuitBreaker.of("transaction-service", config);
        this.notificationServiceCircuitBreaker = CircuitBreaker.of("notification-service", config);
        this.treasuryServiceCircuitBreaker = CircuitBreaker.of("treasury-service", config);
        this.analyticsServiceCircuitBreaker = CircuitBreaker.of("analytics-service", config);
    }
    
    public <T> T executeWithCircuitBreaker(String serviceName, Supplier<T> operation) {
        CircuitBreaker circuitBreaker = getCircuitBreaker(serviceName);
        return circuitBreaker.executeSupplier(operation);
    }
    
    public <T> T executeWithRetryAndCircuitBreaker(String serviceName, Supplier<T> operation) {
        RetryConfig retryConfig = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofSeconds(1))
            .build();
        
        Retry retry = Retry.of(serviceName + "-retry", retryConfig);
        CircuitBreaker circuitBreaker = getCircuitBreaker(serviceName);
        
        Supplier<T> decoratedSupplier = Retry.decorateSupplier(retry, operation);
        return circuitBreaker.executeSupplier(decoratedSupplier);
    }
    
    private CircuitBreaker getCircuitBreaker(String serviceName) {
        switch (serviceName) {
            case "customer-service":
                return customerServiceCircuitBreaker;
            case "account-service":
                return accountServiceCircuitBreaker;
            case "transaction-service":
                return transactionServiceCircuitBreaker;
            case "notification-service":
                return notificationServiceCircuitBreaker;
            case "treasury-service":
                return treasuryServiceCircuitBreaker;
            case "analytics-service":
                return analyticsServiceCircuitBreaker;
            default:
                return customerServiceCircuitBreaker; // default fallback
        }
    }
    
    public CircuitBreaker.State getCircuitBreakerState(String serviceName) {
        return getCircuitBreaker(serviceName).getState();
    }
    
    public CircuitBreaker.Metrics getCircuitBreakerMetrics(String serviceName) {
        return getCircuitBreaker(serviceName).getMetrics();
    }
}
