package com.xypay.analytics.controller;

import com.xypay.analytics.domain.CustomerAnalytics;
import com.xypay.analytics.domain.CustomerSegment;
import com.xypay.analytics.repository.CustomerAnalyticsRepository;
import com.xypay.analytics.repository.CustomerSegmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics/customers")
@RequiredArgsConstructor
public class CustomerAnalyticsController {

    private final CustomerAnalyticsRepository customerAnalyticsRepository;
    private final CustomerSegmentRepository customerSegmentRepository;

    @GetMapping("/{customerId}/analytics")
    public ResponseEntity<CustomerAnalytics> getCustomerAnalytics(@PathVariable Long customerId) {
        return customerAnalyticsRepository.findByCustomerId(customerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{customerId}/segments")
    public ResponseEntity<List<CustomerSegment>> getCustomerSegments(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerSegmentRepository.findByCustomerId(customerId));
    }

    @GetMapping("/{customerId}/clv")
    public ResponseEntity<Map<String, Object>> getCustomerClv(@PathVariable Long customerId) {
        CustomerAnalytics ca = customerAnalyticsRepository.findByCustomerId(customerId).orElse(null);
        BigDecimal clv = ca != null && ca.getTotalTransactionValue() != null ? ca.getTotalTransactionValue() : BigDecimal.ZERO;
        return ResponseEntity.ok(Map.of(
                "customerId", customerId,
                "clv", clv
        ));
    }
}


