package com.xypay.analytics.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics/ops")
@RequiredArgsConstructor
public class OperationalAnalyticsController {

    private final MeterRegistry meterRegistry;

    @GetMapping("/sla")
    public ResponseEntity<Map<String, Object>> sla() {
        Map<String, Object> m = new HashMap<>();
        double httpLatencyP95 = Search.in(meterRegistry).name("http.server.requests").meters().stream()
                .flatMap(mx -> mx.measure().stream())
                .filter(ms -> ms.getStatistic().name().equalsIgnoreCase("percentile") || ms.getStatistic().name().equalsIgnoreCase("VALUE"))
                .mapToDouble(ms -> ms.getValue()).average().orElse(0);
        m.put("httpLatencyApprox", httpLatencyP95);
        return ResponseEntity.ok(m);
    }

    @GetMapping("/errors")
    public ResponseEntity<Map<String, Object>> errors() {
        Map<String, Object> m = new HashMap<>();
        double errorCount = Search.in(meterRegistry).name("http.server.requests").meters().stream()
                .flatMap(mx -> mx.measure().stream())
                .filter(ms -> ms.getStatistic().name().equalsIgnoreCase("COUNT"))
                .mapToDouble(ms -> ms.getValue()).sum();
        m.put("httpRequestsCount", errorCount);
        return ResponseEntity.ok(m);
    }
}


