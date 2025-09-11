package com.xypay.xypay.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracingConfig {
    
    @Bean
    public OpenTelemetry openTelemetry() {
        // Return a no-op OpenTelemetry instance
        return OpenTelemetry.noop();
    }
    
    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("xypay-tracer");
    }
}