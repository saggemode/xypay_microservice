package com.xypay.xypay.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;

@Configuration
public class MonitoringConfig {
    
    @Bean
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }
    
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
            "application", "xypay-web-ui-service",
            "environment", "production"
        );
    }
    
    @Bean
    public Timer customerServiceTimer(MeterRegistry meterRegistry) {
        return Timer.builder("xypay.customer.service.duration")
            .description("Customer service call duration")
            .register(meterRegistry);
    }
    
    @Bean
    public Timer accountServiceTimer(MeterRegistry meterRegistry) {
        return Timer.builder("xypay.account.service.duration")
            .description("Account service call duration")
            .register(meterRegistry);
    }
    
    @Bean
    public Timer transactionServiceTimer(MeterRegistry meterRegistry) {
        return Timer.builder("xypay.transaction.service.duration")
            .description("Transaction service call duration")
            .register(meterRegistry);
    }
    
    @Bean
    public Timer notificationServiceTimer(MeterRegistry meterRegistry) {
        return Timer.builder("xypay.notification.service.duration")
            .description("Notification service call duration")
            .register(meterRegistry);
    }
    
    @Bean
    public Timer treasuryServiceTimer(MeterRegistry meterRegistry) {
        return Timer.builder("xypay.treasury.service.duration")
            .description("Treasury service call duration")
            .register(meterRegistry);
    }
    
    @Bean
    public Timer analyticsServiceTimer(MeterRegistry meterRegistry) {
        return Timer.builder("xypay.analytics.service.duration")
            .description("Analytics service call duration")
            .register(meterRegistry);
    }
}
