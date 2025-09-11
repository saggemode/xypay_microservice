package com.xypay.xypay.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "analytics-service")
public interface AnalyticsServiceClient {
    
    @GetMapping("/api/analytics/dashboard")
    Map<String, Object> getDashboardAnalytics();
    
    @GetMapping("/api/analytics/transactions")
    Map<String, Object> getTransactionAnalytics(@RequestParam(required = false) String period);
    
    @GetMapping("/api/analytics/customers")
    Map<String, Object> getCustomerAnalytics();
    
    @GetMapping("/api/analytics/risk-scores/{customerId}")
    Map<String, Object> getCustomerRiskScore(@PathVariable("customerId") Long customerId);
    
    @PostMapping("/api/analytics/fraud-detection")
    Map<String, Object> detectFraud(@RequestBody Map<String, Object> transactionData);
}
