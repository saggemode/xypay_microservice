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
            mockTransaction.addMetadata("prefunded_from_xysave", "true");
            
            // Process the spending transaction using our comprehensive SpendAndSaveService
            logger.info("Processing Spend and Save for XySave transaction {} - amount: {}", 
                instance.getId(), instance.getAmount());
            var autoSaveTx = spendAndSaveService.processSpendingTransaction(mockTransaction);
            
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
    private static class MockTransaction extends com.xypay.xypay.domain.Transaction {
        private final java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        
        public MockTransaction(XySaveTransaction xysaveTransaction) {
            // Set the required fields for Transaction
            this.setId(xysaveTransaction.getId());
            this.setType("debit"); // XySave withdrawals are debit transactions
            this.setStatus("success");
            this.setAmount(xysaveTransaction.getAmount());
            this.setDescription(xysaveTransaction.getDescription());
            this.setBalanceAfter(xysaveTransaction.getBalanceAfter());
            this.setReference(xysaveTransaction.getReference());
            this.setCreatedAt(xysaveTransaction.getCreatedAt());
            
            // Create a mock wallet that points to the user
            MockWallet mockWallet = new MockWallet();
            mockWallet.user = xysaveTransaction.getXysaveAccount().getUser();
            this.setWallet(mockWallet);
        }
        
        // Store metadata as JSON string to match Transaction.getMetadata() signature
        private String metadataJson = "{}";
        
        @Override
        public String getMetadata() { 
            return metadataJson; 
        }
        
        // Helper method to add metadata
        public void addMetadata(String key, Object value) {
            metadata.put(key, value);
            // Convert to JSON string (simplified for now)
            metadataJson = "{\"" + key + "\":\"" + value + "\"}";
        }
    }
    
    /**
     * Mock wallet class to provide user reference for SpendAndSaveService.
     */
    private static class MockWallet extends com.xypay.xypay.domain.Wallet {
        public com.xypay.xypay.domain.User user;
        
        @Override
        public com.xypay.xypay.domain.User getUser() { 
            return user; 
        }
    }
}
