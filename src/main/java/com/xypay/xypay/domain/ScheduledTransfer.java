package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "scheduled_transfers", indexes = {
    @Index(name = "idx_scheduled_transfer_user", columnList = "user_id"),
    @Index(name = "idx_scheduled_transfer_status", columnList = "status"),
    @Index(name = "idx_scheduled_transfer_next_run", columnList = "next_run_date"),
    @Index(name = "idx_scheduled_transfer_frequency", columnList = "frequency")
})
public class ScheduledTransfer extends BaseEntity {
    
    public enum Status {
        ACTIVE("active"),
        PAUSED("paused"),
        COMPLETED("completed"),
        CANCELLED("cancelled"),
        FAILED("failed");
        
        private final String value;
        
        Status(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    public enum Frequency {
        DAILY("daily"),
        WEEKLY("weekly"),
        MONTHLY("monthly"),
        QUARTERLY("quarterly"),
        YEARLY("yearly");
        
        private final String value;
        
        Frequency(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "title", length = 255, nullable = false)
    private String title;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "recipient_account_number", length = 20, nullable = false)
    private String recipientAccountNumber;
    
    @Column(name = "recipient_name", length = 255)
    private String recipientName;
    
    @Column(name = "recipient_bank_code", length = 10)
    private String recipientBankCode;
    
    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false)
    private Frequency frequency;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "next_run_date", nullable = false)
    private LocalDate nextRunDate;
    
    @Column(name = "last_run_date")
    private LocalDate lastRunDate;
    
    @Column(name = "total_runs")
    private Integer totalRuns = 0;
    
    @Column(name = "successful_runs")
    private Integer successfulRuns = 0;
    
    @Column(name = "failed_runs")
    private Integer failedRuns = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;
    
    @Column(name = "is_internal_transfer")
    private Boolean isInternalTransfer = false;
    
    @Column(name = "auto_retry_failed")
    private Boolean autoRetryFailed = true;
    
    @Column(name = "max_retry_attempts")
    private Integer maxRetryAttempts = 3;
    
    @Column(name = "current_retry_count")
    private Integer currentRetryCount = 0;
    
    @Column(name = "last_failure_reason", columnDefinition = "TEXT")
    private String lastFailureReason;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON metadata
    
    // Constructors
    public ScheduledTransfer() {}
    
    public ScheduledTransfer(User user, String title, String recipientAccountNumber, 
                           BigDecimal amount, Frequency frequency, LocalDate startDate) {
        this.user = user;
        this.title = title;
        this.recipientAccountNumber = recipientAccountNumber;
        this.amount = amount;
        this.frequency = frequency;
        this.startDate = startDate;
        this.nextRunDate = startDate;
    }
    
    // Business methods
    public void pause() {
        this.status = Status.PAUSED;
    }
    
    public void resume() {
        this.status = Status.ACTIVE;
    }
    
    public void cancel() {
        this.status = Status.CANCELLED;
    }
    
    public void markAsCompleted() {
        this.status = Status.COMPLETED;
    }
    
    public void markAsFailed(String reason) {
        this.status = Status.FAILED;
        this.lastFailureReason = reason;
        this.currentRetryCount++;
    }
    
    public void resetRetryCount() {
        this.currentRetryCount = 0;
        this.lastFailureReason = null;
    }
    
    public void incrementRunCount() {
        this.totalRuns++;
    }
    
    public void incrementSuccessfulRuns() {
        this.successfulRuns++;
    }
    
    public void incrementFailedRuns() {
        this.failedRuns++;
    }
    
    public void updateNextRunDate() {
        switch (frequency) {
            case DAILY:
                this.nextRunDate = this.nextRunDate.plusDays(1);
                break;
            case WEEKLY:
                this.nextRunDate = this.nextRunDate.plusWeeks(1);
                break;
            case MONTHLY:
                this.nextRunDate = this.nextRunDate.plusMonths(1);
                break;
            case QUARTERLY:
                this.nextRunDate = this.nextRunDate.plusMonths(3);
                break;
            case YEARLY:
                this.nextRunDate = this.nextRunDate.plusYears(1);
                break;
        }
    }
    
    public boolean isDueForExecution() {
        return status == Status.ACTIVE && 
               nextRunDate != null && 
               nextRunDate.isEqual(LocalDate.now()) || nextRunDate.isBefore(LocalDate.now());
    }
    
    public boolean isExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }
    
    public boolean shouldRetry() {
        return autoRetryFailed && 
               currentRetryCount < maxRetryAttempts && 
               status == Status.FAILED;
    }
    
    public BigDecimal getSuccessRate() {
        if (totalRuns == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(successfulRuns)
                .divide(BigDecimal.valueOf(totalRuns), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}