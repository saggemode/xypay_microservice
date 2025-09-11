package com.xypay.transaction.event;

import com.xypay.transaction.domain.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishTransactionEvent(Transaction transaction) {
        try {
            TransactionEvent event = TransactionEvent.builder()
                    .transactionId(transaction.getId())
                    .accountNumber(transaction.getAccountNumber())
                    .receiverAccountNumber(transaction.getReceiverAccountNumber())
                    .amount(transaction.getAmount())
                    .type(transaction.getType().getCode())
                    .channel(transaction.getChannel().getCode())
                    .status(transaction.getStatus().getCode())
                    .currency(transaction.getCurrency())
                    .timestamp(transaction.getTimestamp())
                    .eventType("TRANSACTION_PROCESSED")
                    .build();
            
            kafkaTemplate.send("transaction-events", event);
            log.info("Published transaction event for transaction: {}", transaction.getId());
        } catch (Exception e) {
            log.error("Failed to publish transaction event: {}", e.getMessage());
        }
    }
    
    public void publishTransactionStatusChanged(Transaction transaction) {
        try {
            TransactionEvent event = TransactionEvent.builder()
                    .transactionId(transaction.getId())
                    .accountNumber(transaction.getAccountNumber())
                    .receiverAccountNumber(transaction.getReceiverAccountNumber())
                    .amount(transaction.getAmount())
                    .type(transaction.getType().getCode())
                    .channel(transaction.getChannel().getCode())
                    .status(transaction.getStatus().getCode())
                    .currency(transaction.getCurrency())
                    .timestamp(transaction.getTimestamp())
                    .eventType("TRANSACTION_STATUS_CHANGED")
                    .build();
            
            kafkaTemplate.send("transaction-events", event);
            log.info("Published transaction status changed event for transaction: {}", transaction.getId());
        } catch (Exception e) {
            log.error("Failed to publish transaction status changed event: {}", e.getMessage());
        }
    }
    
    public void publishTransactionCompleted(Transaction transaction) {
        try {
            TransactionEvent event = TransactionEvent.builder()
                    .transactionId(transaction.getId())
                    .accountNumber(transaction.getAccountNumber())
                    .receiverAccountNumber(transaction.getReceiverAccountNumber())
                    .amount(transaction.getAmount())
                    .type(transaction.getType().getCode())
                    .channel(transaction.getChannel().getCode())
                    .status(transaction.getStatus().getCode())
                    .currency(transaction.getCurrency())
                    .timestamp(transaction.getTimestamp())
                    .eventType("TRANSACTION_COMPLETED")
                    .build();
            
            kafkaTemplate.send("transaction-events", event);
            log.info("Published transaction completed event for transaction: {}", transaction.getId());
        } catch (Exception e) {
            log.error("Failed to publish transaction completed event: {}", e.getMessage());
        }
    }
}
