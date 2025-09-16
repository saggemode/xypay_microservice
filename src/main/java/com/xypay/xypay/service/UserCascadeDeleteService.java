package com.xypay.xypay.service;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for handling cascading deletion of users and all related data.
 * This ensures complete cleanup when a user is deleted.
 */
@Service
public class UserCascadeDeleteService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserCascadeDeleteService.class);
    
    // Core repositories
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    // Transaction and financial repositories
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private BankTransferRepository bankTransferRepository;
    
    // Security and compliance repositories
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserSessionRepository userSessionRepository;
    
    // KYC and verification repositories
    @Autowired
    private KYCProfileRepository kycProfileRepository;
    
    // Savings and investment repositories
    @Autowired
    private XySaveAccountRepository xySaveAccountRepository;
    
    /**
     * Delete a user and all related data in the correct order to avoid foreign key constraints.
     * 
     * @param userId The ID of the user to delete
     * @return true if deletion was successful, false otherwise
     */
    @Transactional
    public boolean deleteUserAndAllRelatedData(UUID userId) {
        try {
            logger.info("Starting cascading deletion for user ID: {}", userId);
            
            // Get the user first to ensure it exists
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                logger.warn("User with ID {} not found", userId);
                return false;
            }
            
            // 1. Delete audit logs first (they reference users but shouldn't block deletion)
            try {
                // Use a custom query to delete audit logs by user
                List<AuditLog> auditLogs = auditLogRepository.findAll().stream()
                    .filter(log -> log.getUser() != null && log.getUser().getId().equals(userId))
                    .collect(java.util.stream.Collectors.toList());
                auditLogRepository.deleteAll(auditLogs);
                logger.info("Deleted {} audit logs for user {}", auditLogs.size(), userId);
            } catch (Exception e) {
                logger.warn("Could not delete audit logs for user {}: {}", userId, e.getMessage());
            }
            
            // 2. Delete notifications
            try {
                List<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
                notificationRepository.deleteAll(notifications);
                logger.info("Deleted {} notifications for user {}", notifications.size(), userId);
            } catch (Exception e) {
                logger.warn("Could not delete notifications for user {}: {}", userId, e.getMessage());
            }
            
            // 3. Delete user sessions
            try {
                List<UserSession> sessions = userSessionRepository.findByUser(user);
                userSessionRepository.deleteAll(sessions);
                logger.info("Deleted {} user sessions for user {}", sessions.size(), userId);
            } catch (Exception e) {
                logger.warn("Could not delete user sessions for user {}: {}", userId, e.getMessage());
            }
            
            // 4. Delete KYC profile
            try {
                Optional<KYCProfile> kycProfile = kycProfileRepository.findByUser(user);
                if (kycProfile.isPresent()) {
                    kycProfileRepository.delete(kycProfile.get());
                    logger.info("Deleted KYC profile for user {}", userId);
                }
            } catch (Exception e) {
                logger.warn("Could not delete KYC profile for user {}: {}", userId, e.getMessage());
            }
            
            // 5. Delete XySave account
            try {
                Optional<XySaveAccount> xySaveAccount = xySaveAccountRepository.findByUserId(userId);
                if (xySaveAccount.isPresent()) {
                    xySaveAccountRepository.delete(xySaveAccount.get());
                    logger.info("Deleted XySave account for user {}", userId);
                }
            } catch (Exception e) {
                logger.warn("Could not delete XySave account for user {}: {}", userId, e.getMessage());
            }
            
            // 6. Delete financial transactions and transfers
            try {
                // Find transactions through wallet
                List<Wallet> userWallets = walletRepository.findByUser(user);
                for (Wallet wallet : userWallets) {
                    // Use a custom query to find all transactions for this wallet
                    List<Transaction> transactions = transactionRepository.findAll().stream()
                        .filter(t -> t.getWallet() != null && t.getWallet().getId().equals(wallet.getId()))
                        .collect(java.util.stream.Collectors.toList());
                    transactionRepository.deleteAll(transactions);
                    logger.info("Deleted {} transactions for wallet {}", transactions.size(), wallet.getId());
                }
            } catch (Exception e) {
                logger.warn("Could not delete transactions for user {}: {}", userId, e.getMessage());
            }
            
            try {
                List<BankTransfer> bankTransfers = bankTransferRepository.findByUser(user);
                bankTransferRepository.deleteAll(bankTransfers);
                logger.info("Deleted {} bank transfers for user {}", bankTransfers.size(), userId);
            } catch (Exception e) {
                logger.warn("Could not delete bank transfers for user {}: {}", userId, e.getMessage());
            }
            
            // 7. Delete wallet (this will cascade to user profile due to @OneToOne cascade)
            try {
                List<Wallet> wallets = walletRepository.findByUser(user);
                walletRepository.deleteAll(wallets);
                logger.info("Deleted {} wallets for user {}", wallets.size(), userId);
            } catch (Exception e) {
                logger.warn("Could not delete wallets for user {}: {}", userId, e.getMessage());
            }
            
            // 8. Finally, delete the user (this will cascade to user profile)
            userRepository.deleteById(userId);
            logger.info("Successfully deleted user with ID: {}", userId);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error during cascading deletion for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete user and related data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if a user can be safely deleted (no critical dependencies).
     * 
     * @param userId The ID of the user to check
     * @return true if user can be deleted, false otherwise
     */
    public boolean canDeleteUser(UUID userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return false;
            }
            
            // Check for any pending transfers
            List<BankTransfer> pendingTransfers = bankTransferRepository.findByUser(user);
            for (BankTransfer transfer : pendingTransfers) {
                if ("PENDING".equals(transfer.getStatus())) {
                    logger.warn("User {} has pending transfer {}, cannot delete", userId, transfer.getId());
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error checking if user {} can be deleted: {}", userId, e.getMessage());
            return false;
        }
    }
}