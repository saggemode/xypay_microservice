package com.xypay.xypay.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuditTrailService {
    public static class AuditLog {
        public final LocalDateTime timestamp;
        public final String event;
        public final String details;
        public final String actor;
        public final Long entityId;
        public AuditLog(Long entityId, String event, String details, String actor) {
            this.timestamp = LocalDateTime.now();
            this.event = event;
            this.details = details;
            this.actor = actor;
            this.entityId = entityId;
        }
        public AuditLog(String event, String details) {
            this(null, event, details, null);
        }
    }
    private final List<AuditLog> logs = Collections.synchronizedList(new ArrayList<>());
    public void logEvent(String event, String details) {
        logs.add(new AuditLog(event, details));
    }
    public void logComplianceAction(Long entityId, String event, String details, String actor) {
        logs.add(new AuditLog(entityId, event, details, actor));
    }
    public void logFinancialTransaction(Long entityId, String event, String details, String actor) {
        logs.add(new AuditLog(entityId, event, details, actor));
    }
    public List<AuditLog> getLogs() {
        return Collections.unmodifiableList(logs);
    }
}