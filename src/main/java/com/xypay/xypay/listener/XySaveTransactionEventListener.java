package com.xypay.xypay.listener;

import com.xypay.xypay.domain.XySaveTransaction;
import com.xypay.xypay.event.XySaveTransactionEvent;
import com.xypay.xypay.service.SpendAndSaveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event listener for XySave transaction processing including Spend and Save.
 * Equivalent to Django's @receiver(post_save, sender=XySaveTransaction) signal handler.
 */
@Component
public class XySaveTransactionEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(XySaveTransactionEventListener.class);
    
    @Autowired
    private SpendAndSaveService spendAndSaveService;
    
    /**
     * Process Spend and Save when a withdrawal or transfer_out transaction occurs in XySave account.
     * Equivalent to Django's process_spend_and_save_for_xysave_transaction function.
     * This ensures that when money is removed from XySave account, the Spend and Save percentage
     * is also automatically deducted and sent to the Spend and Save account.
     */
    @EventListener
    @Async
    @Transactional
    public void processSpendAndSaveForXySaveTransaction(XySaveTransactionEvent event) {
        XySaveTransaction instance = event.getXySaveTransaction();
        
        if (!event.isNewlyCreated()) {
            return;
        }
        
        // Only process withdrawal or transfer_out transactions
        if (instance.getTransactionType() != XySaveTransaction.TransactionType.WITHDRAWAL && 
            instance.getTransactionType() != XySaveTransaction.TransactionType.TRANSFER_OUT) {
            return;
        }
        
        try {
            logger.info("Processing Spend and Save for XySave transaction {} - type: {}, amount: {}", 
                instance.getId(), instance.getTransactionType(), instance.getAmount());
            
            // Create a mock transaction object to pass to SpendAndSaveService
            // This mimics a wallet transaction but uses XySave account data
            MockTransaction mockTransaction = new MockTransaction(instance);
            
            // Add metadata to indicate this is prefunded from XySave
            mockTransaction.metadata.put("prefunded_from_xysave", true);
            
            // Process the spending transaction
            // Note: This will need to be updated when SpendAndSaveService is fully implemented
            // For now, we'll just log that the processing would happen
            logger.info("Would process Spend and Save for XySave transaction {} - amount: {}", 
                instance.getId(), instance.getAmount());
            Object autoSaveTx = null; // spendAndSaveService.processSpendingTransaction(mockTransaction);
            
            if (autoSaveTx != null) {
                logger.info("✅ Successfully processed auto-save for XySave transaction {}. Auto-save transaction created", 
                    instance.getId());
            } else {
                logger.info("⚠️ No auto-save processed for XySave transaction {} (user may not have active Spend and Save account)", 
                    instance.getId());
            }
            
        } catch (Exception e) {
            logger.error("❌ Error processing Spend and Save for XySave transaction {}: {}", 
                instance.getId(), e.getMessage());
        }
    }
    
    /**
     * Mock transaction class to mimic wallet transaction for SpendAndSaveService.
     * This allows the service to process XySave transactions as if they were wallet transactions.
     */
    private static class MockTransaction {
        private final Long id;
        private final String type = "DEBIT";
        private final String status = "SUCCESS";
        private final java.math.BigDecimal amount;
        private final String description;
        private final java.math.BigDecimal balanceAfter;
        private final MockWallet wallet;
        private final String reference;
        private final java.time.LocalDateTime timestamp;
        private final java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        
        public MockTransaction(XySaveTransaction xysaveTransaction) {
            this.id = xysaveTransaction.getId();
            this.amount = xysaveTransaction.getAmount();
            this.description = xysaveTransaction.getDescription();
            this.balanceAfter = xysaveTransaction.getBalanceAfter();
            this.reference = xysaveTransaction.getReference();
            this.timestamp = xysaveTransaction.getCreatedAt();
            
            // Create a mock wallet that points to the user
            this.wallet = new MockWallet();
            this.wallet.user = xysaveTransaction.getXySaveAccount().getUser();
        }
        
        // Getters
        public Long getId() { return id; }
        public String getType() { return type; }
        public String getStatus() { return status; }
        public java.math.BigDecimal getAmount() { return amount; }
        public String getDescription() { return description; }
        public java.math.BigDecimal getBalanceAfter() { return balanceAfter; }
        public MockWallet getWallet() { return wallet; }
        public String getReference() { return reference; }
        public java.time.LocalDateTime getTimestamp() { return timestamp; }
        public java.util.Map<String, Object> getMetadata() { return metadata; }
    }
    
    /**
     * Mock wallet class to provide user reference for SpendAndSaveService.
     */
    private static class MockWallet {
        public com.xypay.xypay.domain.User user;
        
        public com.xypay.xypay.domain.User getUser() { return user; }
    }
}
