package com.xypay.analytics.service;

import com.xypay.analytics.domain.RawEvent;
import com.xypay.analytics.repository.RawEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BatchIngestionService {

    private final IngestionService ingestionService;
    private final RawEventRepository rawEventRepository;

    public List<RawEvent> ingestBatch(String source, String eventType, List<Map<String, Object>> payloads) {
        return payloads.stream()
                .map(payload -> ingestionService.ingest(source, eventType, payload))
                .toList();
    }

    // Skeleton ETL job: runs hourly at minute 10; replace with real external source
    @Scheduled(cron = "0 10 * * * *")
    public void runScheduledEtl() {
        long count = rawEventRepository.count();
        log.info("Scheduled ETL heartbeat - current raw_events count={}", count);
        // Placeholder for: pull from S3/warehouse, transform, load via ingestBatch
    }
}


