package com.xypay.transaction.service;

import com.xypay.transaction.domain.Transaction;
import com.xypay.transaction.enums.TransactionStatus;
import com.xypay.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryService {
    
    private final TransactionRepository transactionRepository;
    private final TransactionProcessingService transactionProcessingService;
    private final TransactionEventPublisher transactionEventPublisher;
    
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MINUTES = 5;
    
    @Scheduled(fixedDelay = 300000) // Run every 5 minutes
    public void retryFailedTransactions() {
        log.info("Starting retry process for failed transactions");
        
        try {
            // Find transactions that are eligible for retry
            List<Transaction> retryableTransactions = findRetryableTransactions();
            
            for (Transaction transaction : retryableTransactions) {
                retryTransactionAsync(transaction);
            }
            
            log.info("Retry process completed. Found {} retryable transactions", retryableTransactions.size());
            
        } catch (Exception e) {
            log.error("Retry process failed: {}", e.getMessage(), e);
        }
    }
    
    private List<Transaction> findRetryableTransactions() {
        // Find transactions that are:
        // 1. Failed
        // 2. Have retry attempts < MAX_RETRY_ATTEMPTS
        // 3. Are older than RETRY_DELAY_MINUTES
        LocalDateTime retryThreshold = LocalDateTime.now().minusMinutes(RETRY_DELAY_MINUTES);
        
        return transactionRepository.findRetryableTransactions(retryThreshold);
    }
    
    @Async
    public CompletableFuture<Void> retryTransactionAsync(Transaction transaction) {
        log.info("Retrying transaction: {} attempt: {}", transaction.getId(), getRetryAttempts(transaction));
        
        try {
            // Increment retry attempts
            incrementRetryAttempts(transaction);
            
            // Mark as processing
            transaction.markAsProcessing();
            transactionRepository.save(transaction);
            
            // Publish retry event
            transactionEventPublisher.publishTransactionRetryEvent(transaction);
            
            // Attempt to reprocess the transaction
            // This would depend on the original transaction type and processing logic
            
            // For now, we'll simulate a retry attempt
            boolean retrySuccess = simulateRetryAttempt(transaction);
            
            if (retrySuccess) {
                // Mark as successful
                transaction.markAsSuccess(transaction.getBalanceAfter());
                transactionRepository.save(transaction);
                
                log.info("Transaction retry successful: {}", transaction.getId());
            } else {
                // Check if we should retry again
                if (getRetryAttempts(transaction) >= MAX_RETRY_ATTEMPTS) {
                    // Max retries reached, mark as permanently failed
                    transaction.markAsFailed();
                    transaction.setMetadata(transaction.getMetadata() + ",\"maxRetriesReached\":true");
                    transactionRepository.save(transaction);
                    
                    log.warn("Transaction permanently failed after {} retries: {}", MAX_RETRY_ATTEMPTS, transaction.getId());
                } else {
                    // Mark as failed but eligible for retry
                    transaction.markAsFailed();
                    transactionRepository.save(transaction);
                    
                    log.info("Transaction retry failed, will retry again: {}", transaction.getId());
                }
            }
            
        } catch (Exception e) {
            log.error("Transaction retry failed: {}", e.getMessage(), e);
            
            // Mark as failed
            transaction.markAsFailed();
            transactionRepository.save(transaction);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    @Transactional
    public void retryTransaction(Long transactionId) {
        log.info("Manual retry requested for transaction: {}", transactionId);
        
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        
        if (!isRetryable(transaction)) {
            throw new RuntimeException("Transaction is not retryable: " + transactionId);
        }
        
        retryTransactionAsync(transaction);
    }
    
    private boolean isRetryable(Transaction transaction) {
        // Check if transaction is eligible for retry
        if (transaction.getStatus() != TransactionStatus.FAILED) {
            return false;
        }
        
        if (getRetryAttempts(transaction) >= MAX_RETRY_ATTEMPTS) {
            return false;
        }
        
        // Check if enough time has passed since last attempt
        LocalDateTime lastAttempt = transaction.getProcessedAt();
        if (lastAttempt != null && lastAttempt.isAfter(LocalDateTime.now().minusMinutes(RETRY_DELAY_MINUTES))) {
            return false;
        }
        
        return true;
    }
    
    private int getRetryAttempts(Transaction transaction) {
        String metadata = transaction.getMetadata();
        if (metadata == null || !metadata.contains("retryAttempts")) {
            return 0;
        }
        
        try {
            // Extract retry attempts from metadata
            String retryAttemptsStr = metadata.substring(metadata.indexOf("retryAttempts\":") + 15);
            retryAttemptsStr = retryAttemptsStr.substring(0, retryAttemptsStr.indexOf(","));
            return Integer.parseInt(retryAttemptsStr);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private void incrementRetryAttempts(Transaction transaction) {
        int currentAttempts = getRetryAttempts(transaction);
        int newAttempts = currentAttempts + 1;
        
        String metadata = transaction.getMetadata();
        if (metadata == null) {
            metadata = "{}";
        }
        
        if (metadata.contains("retryAttempts")) {
            metadata = metadata.replaceAll("\"retryAttempts\":\\d+", "\"retryAttempts\":" + newAttempts);
        } else {
            metadata = metadata.substring(0, metadata.length() - 1) + ",\"retryAttempts\":" + newAttempts + "}";
        }
        
        transaction.setMetadata(metadata);
    }
    
    private boolean simulateRetryAttempt(Transaction transaction) {
        // This is a simplified simulation
        // In reality, this would re-execute the original transaction processing logic
        
        // Simulate 70% success rate for retries
        return Math.random() > 0.3;
    }
    
    public RetryPolicy getRetryPolicy(Transaction transaction) {
        // Define retry policies based on transaction type and failure reason
        if (transaction.getType().isDebit()) {
            return new RetryPolicy(3, 5, 300); // 3 attempts, 5 minutes delay, 5 minutes timeout
        } else {
            return new RetryPolicy(5, 2, 180); // 5 attempts, 2 minutes delay, 3 minutes timeout
        }
    }
    
    public static class RetryPolicy {
        private final int maxAttempts;
        private final int delayMinutes;
        private final int timeoutSeconds;
        
        public RetryPolicy(int maxAttempts, int delayMinutes, int timeoutSeconds) {
            this.maxAttempts = maxAttempts;
            this.delayMinutes = delayMinutes;
            this.timeoutSeconds = timeoutSeconds;
        }
        
        public int getMaxAttempts() { return maxAttempts; }
        public int getDelayMinutes() { return delayMinutes; }
        public int getTimeoutSeconds() { return timeoutSeconds; }
    }
}
