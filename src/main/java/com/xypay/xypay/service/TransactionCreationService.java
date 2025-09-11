 package com.xypay.xypay.service;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.Wallet;
import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.domain.BankTransfer;
import com.xypay.xypay.event.TransactionEvent;
import com.xypay.xypay.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for creating transaction records for bank transfers.
 * Equivalent to Django's create_transaction_records function.
 */
@Service
public class TransactionCreationService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionCreationService.class);
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    /**
     * Create transaction records for both sender and receiver.
     * Matches Django's create_transaction_records function exactly.
     */
    @Transactional
    public TransactionPair createTransactionRecords(Wallet senderWallet, Wallet receiverWallet, 
                                                   BigDecimal amount, BankTransfer transferInstance, 
                                                   String description) {
        try {
            // Create sender transaction (DEBIT)
            Transaction senderTransaction = new Transaction();
            senderTransaction.setWallet(senderWallet);
            senderTransaction.setReceiver(receiverWallet);
            senderTransaction.setReference("TXN-" + System.currentTimeMillis());
            senderTransaction.setAmount(amount);
            senderTransaction.setType("DEBIT");
            senderTransaction.setDirection("DEBIT");
            senderTransaction.setChannel("TRANSFER");
            senderTransaction.setDescription(description);
            senderTransaction.setStatus("SUCCESS");
            senderTransaction.setBalanceAfter(senderWallet.getBalance());
            // Set idempotency key for transaction deduplication
            senderTransaction.setIdempotencyKey("TXN-SEND-" + transferInstance.getId());
            
            // Set metadata for sender transaction (as JSON string)
            try {
                Map<String, Object> senderMetadata = new HashMap<>();
                senderMetadata.put("transfer_id", transferInstance.getId().toString());
                senderMetadata.put("transfer_type", "internal");
                senderMetadata.put("recipient_account", receiverWallet.getAccountNumber());
                senderMetadata.put("recipient_name", getFullNameOrUsername(receiverWallet.getUser()));
                ObjectMapper mapper = new ObjectMapper();
                senderTransaction.setMetadata(mapper.writeValueAsString(senderMetadata));
            } catch (Exception e) {
                logger.warn("Failed to serialize sender metadata: {}", e.getMessage());
                senderTransaction.setMetadata("{}");
            }
            
            senderTransaction = transactionRepository.save(senderTransaction);
            
            // Publish TransactionEvent for sender transaction
            eventPublisher.publishEvent(new TransactionEvent(this, senderTransaction, true));
            
            // Create receiver transaction (CREDIT)
            Transaction receiverTransaction = new Transaction();
            receiverTransaction.setWallet(receiverWallet);
            receiverTransaction.setReceiver(null); // No receiver for credit transactions
            receiverTransaction.setReference("TXN-" + (System.currentTimeMillis() + 1));
            receiverTransaction.setAmount(amount);
            receiverTransaction.setType("CREDIT");
            receiverTransaction.setDirection("CREDIT");
            receiverTransaction.setChannel("TRANSFER");
            receiverTransaction.setDescription("Received " + amount + " from " + senderWallet.getAccountNumber());
            receiverTransaction.setStatus("SUCCESS");
            receiverTransaction.setBalanceAfter(receiverWallet.getBalance());
            // Set idempotency key for transaction deduplication
            receiverTransaction.setIdempotencyKey("TXN-RECV-" + transferInstance.getId());
            
            // Set metadata for receiver transaction (as JSON string)
            try {
                Map<String, Object> receiverMetadata = new HashMap<>();
                receiverMetadata.put("transfer_id", transferInstance.getId().toString());
                receiverMetadata.put("transfer_type", "internal");
                receiverMetadata.put("sender_account", senderWallet.getAccountNumber());
                receiverMetadata.put("sender_name", getFullNameOrUsername(senderWallet.getUser()));
                ObjectMapper mapper = new ObjectMapper();
                receiverTransaction.setMetadata(mapper.writeValueAsString(receiverMetadata));
            } catch (Exception e) {
                logger.warn("Failed to serialize receiver metadata: {}", e.getMessage());
                receiverTransaction.setMetadata("{}");
            }
            
            receiverTransaction = transactionRepository.save(receiverTransaction);
            
            // Publish TransactionEvent for receiver transaction
            eventPublisher.publishEvent(new TransactionEvent(this, receiverTransaction, true));
            
            logger.info("Created transaction records - Sender: {}, Receiver: {}", 
                senderTransaction.getId(), receiverTransaction.getId());
            
            return new TransactionPair(senderTransaction, receiverTransaction);
            
        } catch (Exception e) {
            logger.error("Error creating transaction records: {}", e.getMessage());
            throw new RuntimeException("Failed to create transaction records", e);
        }
    }

    /**
     * Create a single sender transaction for external transfers.
     */
    @Transactional
    public Transaction createExternalSenderTransaction(Wallet senderWallet,
                                                       BigDecimal amount,
                                                       BankTransfer transferInstance,
                                                       String description) {
        try {
            Transaction senderTransaction = new Transaction();
            senderTransaction.setWallet(senderWallet);
            senderTransaction.setReceiver(null);
            senderTransaction.setReference("TXN-" + System.currentTimeMillis());
            senderTransaction.setAmount(amount);
            senderTransaction.setType("DEBIT");
            senderTransaction.setDirection("DEBIT");
            senderTransaction.setChannel("TRANSFER");
            senderTransaction.setDescription(description);
            senderTransaction.setStatus("SUCCESS");
            senderTransaction.setBalanceAfter(senderWallet.getBalance());
            senderTransaction.setIdempotencyKey("TXN-EXT-SEND-" + transferInstance.getId());

            try {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("transfer_id", transferInstance.getId().toString());
                metadata.put("transfer_type", "external");
                metadata.put("recipient_account", transferInstance.getAccountNumber());
                metadata.put("recipient_bank", transferInstance.getBankName());
                ObjectMapper mapper = new ObjectMapper();
                senderTransaction.setMetadata(mapper.writeValueAsString(metadata));
            } catch (Exception e) {
                logger.warn("Failed to serialize external sender metadata: {}", e.getMessage());
                senderTransaction.setMetadata("{}");
            }

            senderTransaction = transactionRepository.save(senderTransaction);
            eventPublisher.publishEvent(new TransactionEvent(this, senderTransaction, true));
            return senderTransaction;
        } catch (Exception e) {
            logger.error("Error creating external sender transaction: {}", e.getMessage());
            throw new RuntimeException("Failed to create external sender transaction", e);
        }
    }
    
    private String getFullNameOrUsername(User user) {
        String fullName = (user.getFirstName() != null ? user.getFirstName() : "") + 
                         " " + (user.getLastName() != null ? user.getLastName() : "");
        fullName = fullName.trim();
        return fullName.isEmpty() ? user.getUsername() : fullName;
    }
    
    /**
     * Helper class to return both transactions
     */
    public static class TransactionPair {
        private final Transaction senderTransaction;
        private final Transaction receiverTransaction;
        
        public TransactionPair(Transaction senderTransaction, Transaction receiverTransaction) {
            this.senderTransaction = senderTransaction;
            this.receiverTransaction = receiverTransaction;
        }
        
        public Transaction getSenderTransaction() {
            return senderTransaction;
        }
        
        public Transaction getReceiverTransaction() {
            return receiverTransaction;
        }
    }
}
