package com.xypay.xypay.service;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class EscrowService {
    
    private static final Logger logger = LoggerFactory.getLogger(EscrowService.class);
    
    @Autowired
    private EscrowAccountRepository escrowAccountRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Create a new escrow account
     */
    public EscrowAccount createEscrowAccount(User buyer, User seller, String title, 
                                           String description, BigDecimal amount, 
                                           LocalDateTime expiryDate) {
        try {
            String escrowId = "ESC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            EscrowAccount escrowAccount = new EscrowAccount(buyer, seller, escrowId, title, 
                                                          description, amount);
            escrowAccount.setExpiryDate(expiryDate);
            
            // Calculate escrow fee (2% of amount, minimum ₦50, maximum ₦5000)
            BigDecimal escrowFee = calculateEscrowFee(amount);
            escrowAccount.setEscrowFee(escrowFee);
            
            EscrowAccount saved = escrowAccountRepository.save(escrowAccount);
            
            // Send notifications
            sendEscrowNotification(buyer, "ESCROW_CREATED", 
                String.format("Escrow account %s created for transaction: %s", escrowId, title));
            
            sendEscrowNotification(seller, "ESCROW_CREATED", 
                String.format("Escrow account %s created for transaction: %s", escrowId, title));
            
            logger.info("Created escrow account {} for buyer {} and seller {}", 
                escrowId, buyer.getUsername(), seller.getUsername());
            
            return saved;
            
        } catch (Exception e) {
            logger.error("Error creating escrow account: {}", e.getMessage());
            throw new RuntimeException("Failed to create escrow account", e);
        }
    }
    
    /**
     * Fund escrow account
     */
    public EscrowAccount fundEscrowAccount(UUID escrowId, User buyer) {
        try {
            EscrowAccount escrowAccount = escrowAccountRepository.findById(escrowId)
                    .orElseThrow(() -> new RuntimeException("Escrow account not found"));
            
            if (!escrowAccount.getBuyer().getId().equals(buyer.getId())) {
                throw new RuntimeException("Only the buyer can fund this escrow account");
            }
            
            if (escrowAccount.getStatus() != EscrowAccount.Status.PENDING) {
                throw new RuntimeException("Escrow account is not in pending status");
            }
            
            // Check buyer's wallet balance
            Wallet buyerWallet = buyer.getWallet();
            if (buyerWallet.getBalance().compareTo(escrowAccount.getTotalAmount()) < 0) {
                throw new RuntimeException("Insufficient balance to fund escrow account");
            }
            
            // Deduct amount from buyer's wallet
            buyerWallet.setBalance(buyerWallet.getBalance().subtract(escrowAccount.getTotalAmount()));
            walletRepository.save(buyerWallet);
            
            // Create transaction record
            Transaction transaction = new Transaction();
            transaction.setWallet(buyerWallet);
            transaction.setAmount(escrowAccount.getTotalAmount());
            transaction.setType("DEBIT");
            transaction.setChannel("ESCROW");
            transaction.setDescription("Escrow funding: " + escrowAccount.getTitle());
            transaction.setReference("ESC-FUND-" + escrowAccount.getEscrowId());
            transaction.setStatus("SUCCESS");
            transaction.setBalanceAfter(buyerWallet.getBalance());
            
            // Mark escrow as funded
            escrowAccount.fund();
            escrowAccount = escrowAccountRepository.save(escrowAccount);
            
            // Send notifications
            sendEscrowNotification(buyer, "ESCROW_FUNDED", 
                String.format("Escrow account %s has been funded", escrowAccount.getEscrowId()));
            
            sendEscrowNotification(escrowAccount.getSeller(), "ESCROW_FUNDED", 
                String.format("Escrow account %s has been funded by buyer", escrowAccount.getEscrowId()));
            
            logger.info("Funded escrow account {}", escrowAccount.getEscrowId());
            
            return escrowAccount;
            
        } catch (Exception e) {
            logger.error("Error funding escrow account {}: {}", escrowId, e.getMessage());
            throw new RuntimeException("Failed to fund escrow account", e);
        }
    }
    
    /**
     * Release escrow funds to seller
     */
    public EscrowAccount releaseEscrowFunds(UUID escrowId, User releaser) {
        try {
            EscrowAccount escrowAccount = escrowAccountRepository.findById(escrowId)
                    .orElseThrow(() -> new RuntimeException("Escrow account not found"));
            
            if (!escrowAccount.getBuyer().getId().equals(releaser.getId()) && 
                !escrowAccount.getSeller().getId().equals(releaser.getId())) {
                throw new RuntimeException("Only buyer or seller can release escrow funds");
            }
            
            if (escrowAccount.getStatus() != EscrowAccount.Status.FUNDED) {
                throw new RuntimeException("Escrow account is not funded");
            }
            
            // Transfer funds to seller
            Wallet sellerWallet = escrowAccount.getSeller().getWallet();
            sellerWallet.setBalance(sellerWallet.getBalance().add(escrowAccount.getAmount()));
            walletRepository.save(sellerWallet);
            
            // Create transaction record for seller
            Transaction sellerTransaction = new Transaction();
            sellerTransaction.setWallet(sellerWallet);
            sellerTransaction.setAmount(escrowAccount.getAmount());
            sellerTransaction.setType("CREDIT");
            sellerTransaction.setChannel("ESCROW");
            sellerTransaction.setDescription("Escrow release: " + escrowAccount.getTitle());
            sellerTransaction.setReference("ESC-REL-" + escrowAccount.getEscrowId());
            sellerTransaction.setStatus("SUCCESS");
            sellerTransaction.setBalanceAfter(sellerWallet.getBalance());
            
            // Set metadata with sender information
            try {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("transaction_type", "escrow_release");
                metadata.put("sender_account", "Escrow System");
                metadata.put("sender_name", "Escrow System");
                metadata.put("escrow_id", escrowAccount.getEscrowId());
                metadata.put("buyer_id", escrowAccount.getBuyer().getId().toString());
                metadata.put("seller_id", escrowAccount.getSeller().getId().toString());
                metadata.put("amount", escrowAccount.getAmount().toString());
                sellerTransaction.setMetadata(objectMapper.writeValueAsString(metadata));
            } catch (Exception e) {
                logger.warn("Failed to serialize escrow release metadata: {}", e.getMessage());
                sellerTransaction.setMetadata("{}");
            }
            
            // Mark escrow as released
            escrowAccount.release();
            escrowAccount = escrowAccountRepository.save(escrowAccount);
            
            // Send notifications
            sendEscrowNotification(escrowAccount.getSeller(), "ESCROW_RELEASED", 
                String.format("Escrow funds for %s have been released to your account", 
                    escrowAccount.getTitle()));
            
            sendEscrowNotification(escrowAccount.getBuyer(), "ESCROW_RELEASED", 
                String.format("Escrow funds for %s have been released to seller", 
                    escrowAccount.getTitle()));
            
            logger.info("Released escrow funds for account {}", escrowAccount.getEscrowId());
            
            return escrowAccount;
            
        } catch (Exception e) {
            logger.error("Error releasing escrow funds {}: {}", escrowId, e.getMessage());
            throw new RuntimeException("Failed to release escrow funds", e);
        }
    }
    
    /**
     * Refund escrow funds to buyer
     */
    public EscrowAccount refundEscrowFunds(UUID escrowId, User refunder, String reason) {
        try {
            EscrowAccount escrowAccount = escrowAccountRepository.findById(escrowId)
                    .orElseThrow(() -> new RuntimeException("Escrow account not found"));
            
            if (!escrowAccount.getBuyer().getId().equals(refunder.getId()) && 
                !escrowAccount.getSeller().getId().equals(refunder.getId())) {
                throw new RuntimeException("Only buyer or seller can refund escrow funds");
            }
            
            if (escrowAccount.getStatus() != EscrowAccount.Status.FUNDED) {
                throw new RuntimeException("Escrow account is not funded");
            }
            
            // Refund funds to buyer
            Wallet buyerWallet = escrowAccount.getBuyer().getWallet();
            buyerWallet.setBalance(buyerWallet.getBalance().add(escrowAccount.getTotalAmount()));
            walletRepository.save(buyerWallet);
            
            // Create transaction record for buyer
            Transaction buyerTransaction = new Transaction();
            buyerTransaction.setWallet(buyerWallet);
            buyerTransaction.setAmount(escrowAccount.getTotalAmount());
            buyerTransaction.setType("CREDIT");
            buyerTransaction.setChannel("ESCROW");
            buyerTransaction.setDescription("Escrow refund: " + escrowAccount.getTitle());
            buyerTransaction.setReference("ESC-REF-" + escrowAccount.getEscrowId());
            buyerTransaction.setStatus("SUCCESS");
            buyerTransaction.setBalanceAfter(buyerWallet.getBalance());
            
            // Set metadata with sender information
            try {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("transaction_type", "escrow_refund");
                metadata.put("sender_account", "Escrow System");
                metadata.put("sender_name", "Escrow System");
                metadata.put("escrow_id", escrowAccount.getEscrowId());
                metadata.put("buyer_id", escrowAccount.getBuyer().getId().toString());
                metadata.put("seller_id", escrowAccount.getSeller().getId().toString());
                metadata.put("amount", escrowAccount.getTotalAmount().toString());
                metadata.put("refund_reason", reason);
                buyerTransaction.setMetadata(objectMapper.writeValueAsString(metadata));
            } catch (Exception e) {
                logger.warn("Failed to serialize escrow refund metadata: {}", e.getMessage());
                buyerTransaction.setMetadata("{}");
            }
            
            // Mark escrow as refunded
            escrowAccount.refund();
            escrowAccount = escrowAccountRepository.save(escrowAccount);
            
            // Send notifications
            sendEscrowNotification(escrowAccount.getBuyer(), "ESCROW_REFUNDED", 
                String.format("Escrow funds for %s have been refunded to your account. Reason: %s", 
                    escrowAccount.getTitle(), reason));
            
            sendEscrowNotification(escrowAccount.getSeller(), "ESCROW_REFUNDED", 
                String.format("Escrow funds for %s have been refunded to buyer. Reason: %s", 
                    escrowAccount.getTitle(), reason));
            
            logger.info("Refunded escrow funds for account {}", escrowAccount.getEscrowId());
            
            return escrowAccount;
            
        } catch (Exception e) {
            logger.error("Error refunding escrow funds {}: {}", escrowId, e.getMessage());
            throw new RuntimeException("Failed to refund escrow funds", e);
        }
    }
    
    /**
     * Raise dispute for escrow account
     */
    public EscrowAccount raiseDispute(UUID escrowId, User disputer, String reason) {
        try {
            EscrowAccount escrowAccount = escrowAccountRepository.findById(escrowId)
                    .orElseThrow(() -> new RuntimeException("Escrow account not found"));
            
            if (!escrowAccount.getBuyer().getId().equals(disputer.getId()) && 
                !escrowAccount.getSeller().getId().equals(disputer.getId())) {
                throw new RuntimeException("Only buyer or seller can raise dispute");
            }
            
            if (escrowAccount.getStatus() != EscrowAccount.Status.FUNDED) {
                throw new RuntimeException("Escrow account is not funded");
            }
            
            // Mark escrow as disputed
            escrowAccount.dispute(disputer.getId(), reason);
            escrowAccount = escrowAccountRepository.save(escrowAccount);
            
            // Send notifications
            sendEscrowNotification(escrowAccount.getBuyer(), "ESCROW_DISPUTED", 
                String.format("Dispute raised for escrow account %s. Reason: %s", 
                    escrowAccount.getEscrowId(), reason));
            
            sendEscrowNotification(escrowAccount.getSeller(), "ESCROW_DISPUTED", 
                String.format("Dispute raised for escrow account %s. Reason: %s", 
                    escrowAccount.getEscrowId(), reason));
            
            logger.info("Dispute raised for escrow account {}", escrowAccount.getEscrowId());
            
            return escrowAccount;
            
        } catch (Exception e) {
            logger.error("Error raising dispute for escrow {}: {}", escrowId, e.getMessage());
            throw new RuntimeException("Failed to raise dispute", e);
        }
    }
    
    /**
     * Check for expired escrow accounts (runs daily)
     */
    @Scheduled(cron = "0 0 1 * * ?") // Run at 1 AM daily
    public void checkExpiredEscrowAccounts() {
        logger.info("Checking for expired escrow accounts");
        
        try {
            List<EscrowAccount> expiredAccounts = escrowAccountRepository
                    .findByStatusAndExpiryDateBefore(EscrowAccount.Status.FUNDED, LocalDateTime.now());
            
            for (EscrowAccount escrowAccount : expiredAccounts) {
                try {
                    // Auto-refund expired escrow accounts
                    refundEscrowFunds(escrowAccount.getId(), escrowAccount.getBuyer(), 
                                    "Escrow account expired");
                    
                    logger.info("Auto-refunded expired escrow account {}", 
                        escrowAccount.getEscrowId());
                        
                } catch (Exception e) {
                    logger.error("Error auto-refunding expired escrow {}: {}", 
                        escrowAccount.getId(), e.getMessage());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error checking expired escrow accounts: {}", e.getMessage());
        }
    }
    
    /**
     * Send escrow notification to user
     */
    private void sendEscrowNotification(User user, String notificationType, String message) {
        try {
            Notification notification = new Notification();
            notification.setRecipient(user);
            notification.setTitle(notificationType.replace("_", " "));
            notification.setMessage(message);
            
            // Map string type to enum
            try {
                notification.setNotificationType(NotificationType.valueOf(notificationType));
            } catch (IllegalArgumentException e) {
                notification.setNotificationType(NotificationType.SYSTEM_ALERT);
            }
            
            notification.setLevel(NotificationLevel.INFO);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSource("escrow");
            
            notificationRepository.save(notification);
            logger.info("Escrow notification sent to user {}: {}", user.getId(), message);
            
        } catch (Exception e) {
            logger.error("Failed to send escrow notification to user {}: {}", user.getId(), e.getMessage());
        }
    }
    
    /**
     * Calculate escrow fee
     */
    private BigDecimal calculateEscrowFee(BigDecimal amount) {
        BigDecimal fee = amount.multiply(new BigDecimal("0.02")); // 2%
        
        // Minimum fee: ₦50
        BigDecimal minFee = new BigDecimal("50.00");
        if (fee.compareTo(minFee) < 0) {
            fee = minFee;
        }
        
        // Maximum fee: ₦5000
        BigDecimal maxFee = new BigDecimal("5000.00");
        if (fee.compareTo(maxFee) > 0) {
            fee = maxFee;
        }
        
        return fee;
    }
    
    /**
     * Get escrow accounts by user
     */
    public List<EscrowAccount> getEscrowAccountsByUser(User user) {
        return escrowAccountRepository.findByBuyerOrSellerOrderByCreatedAtDesc(user, user);
    }
    
    /**
     * Get escrow account by ID
     */
    public EscrowAccount getEscrowAccount(UUID id) {
        return escrowAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Escrow account not found"));
    }
}
