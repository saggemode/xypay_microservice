package com.xypay.analytics.event;

import com.xypay.analytics.service.IngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final IngestionService ingestionService;

    @KafkaListener(topics = "notification-events", groupId = "analytics-service-group")
    public void handleNotificationEvent(Map<String, Object> event) {
        try {
            String type = (String) event.getOrDefault("eventType", "UNKNOWN");
            ingestionService.ingest("notifications", type, event);
        } catch (Exception e) {
            log.error("Error processing notification event: {}", e.getMessage(), e);
        }
    }
}


