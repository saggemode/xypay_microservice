package com.xypay.analytics.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xypay.analytics.domain.RawEvent;
import com.xypay.analytics.repository.RawEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IngestionService {
    private final RawEventRepository rawEventRepository;
    private final ObjectMapper objectMapper;
    private final com.xypay.analytics.util.PiiMaskingUtil pii = null;

    public RawEvent ingest(String source, String eventType, Object payload) {
        try {
            RawEvent event = new RawEvent();
            event.setSource(source);
            event.setEventType(eventType);
            if (payload instanceof java.util.Map) {
                com.xypay.analytics.util.PiiMaskingUtil.maskInPlace((java.util.Map<String, Object>) payload);
            }
            String json = payload instanceof String ? (String) payload : objectMapper.writeValueAsString(payload);
            event.setPayload(json);
            RawEvent saved = rawEventRepository.save(event);
            log.debug("Ingested event: {}:{} id={}", source, eventType, saved.getId());
            return saved;
        } catch (Exception e) {
            throw new RuntimeException("Failed to ingest event: " + e.getMessage(), e);
        }
    }
}


