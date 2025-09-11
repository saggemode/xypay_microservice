package com.xypay.xypay.listener;

import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.event.TransactionEvent;
import com.xypay.xypay.service.SpendAndSaveService;
import com.xypay.xypay.service.XySaveAutoSweepService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event listener for transaction processing including Spend and Save and XySave auto-sweep.
 * Equivalent to Django's @receiver(post_save, sender=Transaction) signal handlers.
 */
@Component
public class TransactionEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionEventListener.class);
    
    @Autowired
    private SpendAndSaveService spendAndSaveService;
    
    @Autowired
    private XySaveAutoSweepService xySaveAutoSweepService;
    
    /**
     * Automatically process Spend and Save when a debit transaction is created.
     * Equivalent to Django's process_spend_and_save_on_transaction function.
     */
    @EventListener
    @Async
    @Transactional
    public void processSpendAndSaveOnTransaction(TransactionEvent event) {
        Transaction instance = event.getTransaction();
        
        logger.info("🔍 Signal triggered for transaction {} - created: {}, type: {}, status: {}", 
            instance.getId(), event.isNewlyCreated(), instance.getType(), instance.getStatus());
        
        if (!event.isNewlyCreated()) {
            logger.info("⏭️ Skipping - not a new transaction");
            return;
        }
        
        // Only process debit transactions (spending transactions)
        if (!"DEBIT".equals(instance.getDirection())) {
            logger.info("⏭️ Skipping - not a debit transaction (direction: {})", instance.getDirection());
            return;
        }
        
        // Only process successful transactions
        if (!"SUCCESS".equals(instance.getStatus())) {
            logger.info("⏭️ Skipping - not a successful transaction (status: {})", instance.getStatus());
            return;
        }
        
        try {
            logger.info("✅ Processing Spend and Save for transaction {} - amount: {}", 
                instance.getId(), instance.getAmount());
            
            // Process the spending transaction for auto-save
            Object autoSaveTx = spendAndSaveService.processSpendingTransaction(instance);
            
            if (autoSaveTx != null) {
                logger.info("✅ Successfully processed auto-save for transaction {}. Auto-save transaction created", 
                    instance.getId());
            } else {
                logger.info("⚠️ No auto-save processed for transaction {} (user may not have active Spend and Save account)", 
                    instance.getId());
            }
            
        } catch (Exception e) {
            logger.error("❌ Error processing Spend and Save for transaction {}: {}", 
                instance.getId(), e.getMessage());
            // Don't fail the transaction if Spend and Save processing fails
        }
    }
    
    /**
     * Auto-sweep wallet credits to XySave when enabled.
     * Equivalent to Django's auto_sweep_to_xysave_on_credit function.
     */
    @EventListener
    @Async
    @Transactional
    public void autoSweepToXySaveOnCredit(TransactionEvent event) {
        Transaction instance = event.getTransaction();
        
        if (!event.isNewlyCreated()) {
            return;
        }
        
        if (!"CREDIT".equals(instance.getDirection())) {
            return;
        }
        
        if (!"SUCCESS".equals(instance.getStatus())) {
            return;
        }
        
        try {
            boolean autoSweepEnabled = xySaveAutoSweepService.isAutoSweepEnabled(instance.getWallet().getUser());
            if (!autoSweepEnabled) {
                return;
            }
            
            logger.info("Auto-sweeping {} from wallet {} to XySave for user {}", 
                instance.getAmount(), instance.getWallet().getAccountNumber(), instance.getWallet().getUser().getId());
            
            // Deposit full credited amount to XySave (sweeps from wallet)
            xySaveAutoSweepService.depositToXySave(
                instance.getWallet().getUser(),
                instance.getAmount(),
                "Auto-sweep from wallet credit " + instance.getReference()
            );
            
        } catch (Exception e) {
            logger.error("Failed to auto-sweep credit to XySave for transaction {}: {}", 
                instance.getId(), e.getMessage());
        }
    }
}
