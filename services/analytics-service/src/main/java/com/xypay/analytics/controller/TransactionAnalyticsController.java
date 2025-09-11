package com.xypay.analytics.controller;

import com.xypay.analytics.domain.TransactionAnalytics;
import com.xypay.analytics.repository.TransactionAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics/transactions")
@RequiredArgsConstructor
public class TransactionAnalyticsController {

    private final TransactionAnalyticsRepository transactionAnalyticsRepository;

    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> kpis(@RequestParam(required = false) String currency,
                                                    @RequestParam(required = false) String type,
                                                    @RequestParam(required = false) String channel) {
        BigDecimal avg = transactionAnalyticsRepository.getAverageTransactionAmount();
        BigDecimal totalByCurrency = currency != null ? transactionAnalyticsRepository.getTotalAmountByCurrency(currency) : null;
        Long countByType = type != null ? transactionAnalyticsRepository.countByTransactionType(type) : null;
        int countByChannel = channel != null ? transactionAnalyticsRepository.findByChannel(channel).size() : 0;
        return ResponseEntity.ok(Map.of(
                "averageAmount", avg == null ? BigDecimal.ZERO : avg,
                "totalByCurrency", totalByCurrency == null ? BigDecimal.ZERO : totalByCurrency,
                "countByType", countByType == null ? 0L : countByType,
                "countByChannel", countByChannel
        ));
    }

    @GetMapping("/by-channel")
    public ResponseEntity<List<TransactionAnalytics>> byChannel(@RequestParam String channel) {
        return ResponseEntity.ok(transactionAnalyticsRepository.findByChannel(channel));
    }

    @GetMapping("/by-type")
    public ResponseEntity<List<TransactionAnalytics>> byType(@RequestParam String type) {
        return ResponseEntity.ok(transactionAnalyticsRepository.findByTransactionType(type));
    }
}


