package com.xypay.analytics.service;

import com.xypay.analytics.domain.DailyAggregate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${analytics.alerts.volume-threshold:100000}")
    private BigDecimal volumeThreshold;

    @Value("${analytics.alerts.count-threshold:1000}")
    private Long countThreshold;

    public void evaluateAndEmitDailyAlerts(DailyAggregate aggregate) {
        if (aggregate == null) {
            return;
        }

        boolean volumeExceeded = aggregate.getTransactionsVolume() != null
                && aggregate.getTransactionsVolume().compareTo(volumeThreshold) > 0;
        boolean countExceeded = aggregate.getTransactionsCount() != null
                && aggregate.getTransactionsCount() > countThreshold;

        if (volumeExceeded || countExceeded) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "DAILY_THRESHOLD_EXCEEDED");
            alert.put("date", aggregate.getDate());
            alert.put("transactionsCount", aggregate.getTransactionsCount());
            alert.put("transactionsVolume", aggregate.getTransactionsVolume());
            alert.put("volumeThreshold", volumeThreshold);
            alert.put("countThreshold", countThreshold);

            String payload = toJson(alert);
            log.warn("Emitting analytics alert: {}", payload);
            try {
                kafkaTemplate.send("analytics-alerts", payload);
            } catch (Exception e) {
                log.error("Failed to publish analytics alert: {}", e.getMessage(), e);
            }
        }
    }

    private String toJson(Map<String, Object> data) {
        try {
            // Lightweight manual JSON to avoid ObjectMapper dependency here
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (!first) sb.append(',');
                sb.append('"').append(entry.getKey()).append('"').append(':');
                Object value = entry.getValue();
                if (value == null) {
                    sb.append("null");
                } else if (value instanceof Number || value instanceof Boolean) {
                    sb.append(String.valueOf(value));
                } else {
                    sb.append('"').append(String.valueOf(value).replace("\"", "\\\"")).append('"');
                }
                first = false;
            }
            sb.append('}');
            return sb.toString();
        } catch (Exception ignored) {
            return "{}";
        }
    }
}


