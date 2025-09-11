package com.xypay.analytics.controller;

import com.xypay.analytics.domain.DailyAggregate;
import com.xypay.analytics.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping("/kpis/today")
    public ResponseEntity<Map<String, Object>> getTodayKpis() {
        return ResponseEntity.ok(metricsService.getTodayKpis());
    }

    @GetMapping("/daily")
    public ResponseEntity<List<DailyAggregate>> getDailyAggregates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(metricsService.getAggregates(start, end));
    }
}


