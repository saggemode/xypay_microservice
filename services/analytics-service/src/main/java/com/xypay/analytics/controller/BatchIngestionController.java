package com.xypay.analytics.controller;

import com.xypay.analytics.domain.RawEvent;
import com.xypay.analytics.service.BatchIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics/batch")
@RequiredArgsConstructor
public class BatchIngestionController {

    private final BatchIngestionService batchIngestionService;

    @PostMapping("/ingest/{source}/{eventType}")
    public ResponseEntity<List<RawEvent>> ingestBatch(
            @PathVariable String source,
            @PathVariable String eventType,
            @RequestBody List<Map<String, Object>> payloads) {
        return ResponseEntity.ok(batchIngestionService.ingestBatch(source, eventType, payloads));
    }
}


