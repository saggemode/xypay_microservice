package com.xypay.analytics.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xypay.analytics.domain.DailyAggregate;
import com.xypay.analytics.domain.RawEvent;
import com.xypay.analytics.repository.DailyAggregateRepository;
import com.xypay.analytics.repository.RawEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AggregationService {

    private final RawEventRepository rawEventRepository;
    private final DailyAggregateRepository dailyAggregateRepository;
    private final ObjectMapper objectMapper;
    private final AlertService alertService;

    @Scheduled(cron = "0 5 * * * *")
    public void aggregateDailyMetrics() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        List<RawEvent> events = rawEventRepository.findByDateRange(start, end);
        DailyAggregate aggregate = dailyAggregateRepository.findByDate(today)
                .orElseGet(() -> {
                    DailyAggregate d = new DailyAggregate();
                    d.setDate(today);
                    return d;
                });

        long txCount = events.stream().filter(e -> "transactions".equals(e.getSource())).count();
        BigDecimal txVolume = events.stream()
                .filter(e -> "transactions".equals(e.getSource()))
                .map(e -> parseAmount(e.getPayload()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long activeUsers = events.stream()
                .map(e -> extractUserId(e.getPayload()))
                .filter(id -> id != null)
                .distinct()
                .count();

        aggregate.setTransactionsCount(txCount);
        aggregate.setTransactionsVolume(txVolume);
        aggregate.setActiveUsers(activeUsers);

        dailyAggregateRepository.save(aggregate);
        alertService.evaluateAndEmitDailyAlerts(aggregate);
        log.info("Aggregated daily metrics date={} txCount={} volume={} activeUsers={}", today, txCount, txVolume, activeUsers);
    }

    private BigDecimal parseAmount(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            if (node.has("amount")) {
                return new BigDecimal(node.get("amount").asText("0"));
            }
        } catch (Exception ignored) {}
        return BigDecimal.ZERO;
    }

    private Long extractUserId(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            if (node.has("userId")) {
                return node.get("userId").asLong();
            }
            if (node.has("customerId")) {
                return node.get("customerId").asLong();
            }
        } catch (Exception ignored) {}
        return null;
    }
}


