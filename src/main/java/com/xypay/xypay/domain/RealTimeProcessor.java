package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "real_time_processors")
public class RealTimeProcessor extends BaseEntity {
    
    @Column(name = "processor_name", length = 100, nullable = false)
    private String processorName;
    
    @Column(name = "processor_type")
    @Enumerated(EnumType.STRING)
    private ProcessorType processorType;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "max_throughput_per_second")
    private Long maxThroughputPerSecond = 1000L; // Transactions per second
    
    @Column(name = "current_load")
    private Long currentLoad = 0L;
    
    @Column(name = "queue_size")
    private Long queueSize = 0L;
    
    @Column(name = "max_queue_size")
    private Long maxQueueSize = 10000L;
    
    @Column(name = "processing_time_ms")
    private Long processingTimeMs = 100L; // Average processing time
    
    @Column(name = "success_rate", precision = 5, scale = 2)
    private BigDecimal successRate = new BigDecimal("99.99");
    
    @Column(name = "error_rate", precision = 5, scale = 2)
    private BigDecimal errorRate = new BigDecimal("0.01");
    
    @Column(name = "last_health_check")
    private LocalDateTime lastHealthCheck;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ProcessorStatus status = ProcessorStatus.HEALTHY;
    
    // 24/7 Operations
    @Column(name = "supports_24x7")
    private Boolean supports24x7 = true;
    
    @Column(name = "maintenance_window_start", length = 8)
    private String maintenanceWindowStart; // HH:MM:SS format
    
    @Column(name = "maintenance_window_end", length = 8)
    private String maintenanceWindowEnd;
    
    @Column(name = "auto_failover_enabled")
    private Boolean autoFailoverEnabled = true;
    
    @Column(name = "backup_processor_id")
    private UUID backupProcessorId;
    
    // Performance Metrics
    @Column(name = "total_transactions_processed")
    private Long totalTransactionsProcessed = 0L;
    
    @Column(name = "transactions_today")
    private Long transactionsToday = 0L;
    
    @Column(name = "peak_tps_today")
    private Long peakTpsToday = 0L;
    
    @Column(name = "average_response_time_ms")
    private Long averageResponseTimeMs = 50L;
    
    // Circuit Breaker Pattern
    @Column(name = "circuit_breaker_enabled")
    private Boolean circuitBreakerEnabled = true;
    
    @Column(name = "failure_threshold")
    private Integer failureThreshold = 5;
    
    @Column(name = "recovery_timeout_seconds")
    private Integer recoveryTimeoutSeconds = 60;
    
    @Column(name = "circuit_state")
    @Enumerated(EnumType.STRING)
    private CircuitState circuitState = CircuitState.CLOSED;
    
    public enum ProcessorType {
        PAYMENT_PROCESSOR, TRANSACTION_VALIDATOR, FRAUD_DETECTOR, 
        COMPLIANCE_CHECKER, NOTIFICATION_SENDER, REPORTING_ENGINE,
        WORKFLOW_ENGINE, STP_PROCESSOR
    }
    
    public enum ProcessorStatus {
        HEALTHY, DEGRADED, CRITICAL, OFFLINE, MAINTENANCE
    }
    
    public enum CircuitState {
        CLOSED, OPEN, HALF_OPEN
    }
}
