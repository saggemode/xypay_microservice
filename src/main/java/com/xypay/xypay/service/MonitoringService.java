package com.xypay.xypay.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MonitoringService {
    
    private final MeterRegistry meterRegistry;
    private final Map<String, Counter> serviceCallCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> serviceErrorCounters = new ConcurrentHashMap<>();
    private final Map<String, Timer> serviceTimers = new ConcurrentHashMap<>();
    
    @Autowired
    public MonitoringService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    public void recordServiceCall(String serviceName, String operation, boolean success) {
        // Record service call counter
        String counterName = "xypay.service.calls";
        Counter counter = serviceCallCounters.computeIfAbsent(counterName, 
            name -> Counter.builder(name)
                .tag("service", serviceName)
                .tag("operation", operation)
                .tag("status", success ? "success" : "error")
                .register(meterRegistry));
        counter.increment();
        
        // Record error counter if failed
        if (!success) {
            String errorCounterName = "xypay.service.errors";
            Counter errorCounter = serviceErrorCounters.computeIfAbsent(errorCounterName,
                name -> Counter.builder(name)
                    .tag("service", serviceName)
                    .tag("operation", operation)
                    .register(meterRegistry));
            errorCounter.increment();
        }
    }
    
    public Timer.Sample startServiceTimer(String serviceName, String operation) {
        String timerName = "xypay.service.duration";
        Timer timer = serviceTimers.computeIfAbsent(timerName,
            name -> Timer.builder(name)
                .tag("service", serviceName)
                .tag("operation", operation)
                .register(meterRegistry));
        return Timer.start(meterRegistry);
    }
    
    public void recordServiceDuration(Timer.Sample sample, String serviceName, String operation) {
        String timerName = "xypay.service.duration";
        Timer timer = serviceTimers.computeIfAbsent(timerName,
            name -> Timer.builder(name)
                .tag("service", serviceName)
                .tag("operation", operation)
                .register(meterRegistry));
        sample.stop(timer);
    }
    
    public void recordCircuitBreakerState(String serviceName, String state) {
        Counter.builder("xypay.circuit.breaker.state")
            .tag("service", serviceName)
            .tag("state", state)
            .register(meterRegistry)
            .increment();
    }
    
    public void recordUserAction(String action, String userId) {
        Counter.builder("xypay.user.actions")
            .tag("action", action)
            .tag("user_id", userId)
            .register(meterRegistry)
            .increment();
    }
    
    public void recordPageView(String page, String userId) {
        Counter.builder("xypay.page.views")
            .tag("page", page)
            .tag("user_id", userId)
            .register(meterRegistry)
            .increment();
    }
    
    public void recordError(String errorType, String service, String operation) {
        Counter.builder("xypay.errors")
            .tag("error_type", errorType)
            .tag("service", service)
            .tag("operation", operation)
            .register(meterRegistry)
            .increment();
    }
}
