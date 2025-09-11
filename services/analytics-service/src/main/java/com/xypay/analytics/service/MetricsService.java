package com.xypay.analytics.service;

import com.xypay.analytics.domain.DailyAggregate;
import com.xypay.analytics.repository.DailyAggregateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final DailyAggregateRepository dailyAggregateRepository;

    public Map<String, Object> getTodayKpis() {
        LocalDate today = LocalDate.now();
        DailyAggregate agg = dailyAggregateRepository.findByDate(today)
                .orElseGet(() -> {
                    DailyAggregate d = new DailyAggregate();
                    d.setDate(today);
                    return d;
                });

        Map<String, Object> kpis = new HashMap<>();
        kpis.put("date", agg.getDate());
        kpis.put("transactionsCount", agg.getTransactionsCount());
        kpis.put("transactionsVolume", agg.getTransactionsVolume() != null ? agg.getTransactionsVolume() : BigDecimal.ZERO);
        kpis.put("activeUsers", agg.getActiveUsers());
        return kpis;
    }

    public List<DailyAggregate> getAggregates(LocalDate start, LocalDate end) {
        return dailyAggregateRepository.findByDateBetweenOrderByDateAsc(start, end);
    }
}


