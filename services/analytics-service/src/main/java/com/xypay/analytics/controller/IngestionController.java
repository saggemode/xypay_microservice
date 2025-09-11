package com.xypay.analytics.controller;

import com.xypay.analytics.domain.RawEvent;
import com.xypay.analytics.service.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics/ingest")
@RequiredArgsConstructor
public class IngestionController {

    private final IngestionService ingestionService;

    @PostMapping("/{source}/{eventType}")
    public ResponseEntity<RawEvent> ingestGeneric(
            @PathVariable String source,
            @PathVariable String eventType,
            @RequestBody(required = false) Map<String, Object> payload) {
        RawEvent saved = ingestionService.ingest(source, eventType, payload != null ? payload : Map.of());
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/transactions/{eventType}")
    public ResponseEntity<RawEvent> ingestTransaction(@PathVariable String eventType,
                                                      @RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(ingestionService.ingest("transactions", eventType, payload));
    }

    @PostMapping("/accounts/{eventType}")
    public ResponseEntity<RawEvent> ingestAccount(@PathVariable String eventType,
                                                  @RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(ingestionService.ingest("accounts", eventType, payload));
    }

    @PostMapping("/notifications/{eventType}")
    public ResponseEntity<RawEvent> ingestNotification(@PathVariable String eventType,
                                                       @RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(ingestionService.ingest("notifications", eventType, payload));
    }
}


