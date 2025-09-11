package com.xypay.analytics.event;

import com.xypay.analytics.service.PredictiveAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventListener {
    
    private final PredictiveAnalyticsService predictiveAnalyticsService;
    
    @KafkaListener(topics = "transaction-events", groupId = "analytics-service-group")
    public void handleTransactionEvent(TransactionEvent event) {
        try {
            log.info("Received transaction event: {} for transaction: {}", 
                    event.getEventType(), event.getTransactionId());
            
            switch (event.getEventType()) {
                case "TRANSACTION_CREATED":
                    handleTransactionCreated(event);
                    break;
                case "TRANSACTION_STATUS_CHANGED":
                    handleTransactionStatusChanged(event);
                    break;
                case "TRANSACTION_COMPLETED":
                    handleTransactionCompleted(event);
                    break;
                default:
                    log.warn("Unknown transaction event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing transaction event: {}", e.getMessage(), e);
        }
    }
    
    private void handleTransactionCreated(TransactionEvent event) {
        log.info("Processing transaction created event for transaction: {}", event.getTransactionId());
        // Update real-time analytics
        // Generate fraud risk assessment
        // Update customer behavior analytics
    }
    
    private void handleTransactionStatusChanged(TransactionEvent event) {
        log.info("Processing transaction status changed event for transaction: {}", event.getTransactionId());
        // Update transaction analytics
        // Update risk metrics
    }
    
    private void handleTransactionCompleted(TransactionEvent event) {
        log.info("Processing transaction completed event for transaction: {}", event.getTransactionId());
        // Finalize analytics
        // Update customer segments
        // Generate insights
    }
    
    // TransactionEvent class for deserialization
    public static class TransactionEvent {
        public Long transactionId;
        public Long walletId;
        public Long receiverId;
        public String reference;
        public java.math.BigDecimal amount;
        public String type;
        public String channel;
        public String description;
        public String status;
        public java.math.BigDecimal balanceAfter;
        public String currency;
        public String metadata;
        public String idempotencyKey;
        public String direction;
        public java.time.LocalDateTime processedAt;
        public java.time.LocalDateTime timestamp;
        public String eventType;
        public java.time.LocalDateTime eventTimestamp;
        
        public Long getTransactionId() {
            return transactionId;
        }
        
        public Long getWalletId() {
            return walletId;
        }
        
        public Long getReceiverId() {
            return receiverId;
        }
        
        public String getReference() {
            return reference;
        }
        
        public java.math.BigDecimal getAmount() {
            return amount;
        }
        
        public String getType() {
            return type;
        }
        
        public String getChannel() {
            return channel;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getStatus() {
            return status;
        }
        
        public java.math.BigDecimal getBalanceAfter() {
            return balanceAfter;
        }
        
        public String getCurrency() {
            return currency;
        }
        
        public String getMetadata() {
            return metadata;
        }
        
        public String getIdempotencyKey() {
            return idempotencyKey;
        }
        
        public String getDirection() {
            return direction;
        }
        
        public java.time.LocalDateTime getProcessedAt() {
            return processedAt;
        }
        
        public java.time.LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public String getEventType() {
            return eventType;
        }
        
        public java.time.LocalDateTime getEventTimestamp() {
            return eventTimestamp;
        }
    }
}