package com.xypay.transaction.service;

import com.xypay.transaction.client.AccountServiceClient;
import com.xypay.transaction.domain.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservedBalanceService {
    
    private final AccountServiceClient accountServiceClient;
    
    @Transactional
    public ReservedBalanceResult reserveBalance(String accountNumber, BigDecimal amount, String reference, String reason) {
        log.info("Reserving balance for account: {} amount: {} reference: {}", accountNumber, amount, reference);
        
        try {
            // Check available balance
            BigDecimal availableBalance = getAvailableBalance(accountNumber);
            
            if (availableBalance.compareTo(amount) < 0) {
                return ReservedBalanceResult.failed("Insufficient available balance");
            }
            
            // Create hold request
            AccountServiceClient.HoldRequest holdRequest = new AccountServiceClient.HoldRequest(
                amount,
                reference,
                reason,
                "TRANSACTION_HOLD"
            );
            
            // Reserve the balance
            var response = accountServiceClient.holdAmount(accountNumber, holdRequest);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String holdId = UUID.randomUUID().toString();
                return ReservedBalanceResult.success(holdId, amount, "Balance reserved successfully");
            } else {
                return ReservedBalanceResult.failed("Failed to reserve balance");
            }
            
        } catch (Exception e) {
            log.error("Balance reservation failed: {}", e.getMessage(), e);
            return ReservedBalanceResult.failed("Balance reservation failed: " + e.getMessage());
        }
    }
    
    @Transactional
    public ReservedBalanceResult releaseReservedBalance(String accountNumber, String holdId, String reference, String reason) {
        log.info("Releasing reserved balance for account: {} holdId: {}", accountNumber, holdId);
        
        try {
            // Create release request
            AccountServiceClient.ReleaseRequest releaseRequest = new AccountServiceClient.ReleaseRequest(
                BigDecimal.ZERO, // Amount will be determined by holdId
                reference,
                reason,
                "TRANSACTION_RELEASE"
            );
            
            // Release the reserved balance
            var response = accountServiceClient.releaseAmount(accountNumber, releaseRequest);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return ReservedBalanceResult.success(holdId, BigDecimal.ZERO, "Reserved balance released successfully");
            } else {
                return ReservedBalanceResult.failed("Failed to release reserved balance");
            }
            
        } catch (Exception e) {
            log.error("Balance release failed: {}", e.getMessage(), e);
            return ReservedBalanceResult.failed("Balance release failed: " + e.getMessage());
        }
    }
    
    @Transactional
    public ReservedBalanceResult processReservedTransaction(Transaction transaction) {
        log.info("Processing reserved transaction: {} amount: {}", transaction.getReference(), transaction.getAmount());
        
        try {
            String holdId = UUID.randomUUID().toString();
            
            // Reserve balance for the transaction
            ReservedBalanceResult reserveResult = reserveBalance(
                transaction.getAccountNumber(),
                transaction.getAmount(),
                transaction.getReference(),
                "Transaction processing hold"
            );
            
            if (!reserveResult.isSuccess()) {
                return reserveResult;
            }
            
            // Process the transaction
            // This would integrate with your transaction processing logic
            
            // If transaction succeeds, the reserved balance is automatically converted to debit
            // If transaction fails, release the reserved balance
            
            return ReservedBalanceResult.success(holdId, transaction.getAmount(), "Transaction processed with reserved balance");
            
        } catch (Exception e) {
            log.error("Reserved transaction processing failed: {}", e.getMessage(), e);
            return ReservedBalanceResult.failed("Reserved transaction processing failed: " + e.getMessage());
        }
    }
    
    public BigDecimal getAvailableBalance(String accountNumber) {
        try {
            var response = accountServiceClient.getAccountBalance(accountNumber);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> balanceData = response.getBody();
                BigDecimal ledgerBalance = new BigDecimal(balanceData.get("balance").toString());
                BigDecimal reservedBalance = new BigDecimal(balanceData.getOrDefault("reservedBalance", "0").toString());
                
                return ledgerBalance.subtract(reservedBalance);
            }
            
            return BigDecimal.ZERO;
            
        } catch (Exception e) {
            log.error("Failed to get available balance: {}", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }
    
    public BigDecimal getReservedBalance(String accountNumber) {
        try {
            var response = accountServiceClient.getAccountBalance(accountNumber);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> balanceData = response.getBody();
                return new BigDecimal(balanceData.getOrDefault("reservedBalance", "0").toString());
            }
            
            return BigDecimal.ZERO;
            
        } catch (Exception e) {
            log.error("Failed to get reserved balance: {}", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }
    
    public static class ReservedBalanceResult {
        private final boolean success;
        private final String holdId;
        private final BigDecimal amount;
        private final String message;
        private final String errorCode;
        
        private ReservedBalanceResult(boolean success, String holdId, BigDecimal amount, String message, String errorCode) {
            this.success = success;
            this.holdId = holdId;
            this.amount = amount;
            this.message = message;
            this.errorCode = errorCode;
        }
        
        public static ReservedBalanceResult success(String holdId, BigDecimal amount, String message) {
            return new ReservedBalanceResult(true, holdId, amount, message, null);
        }
        
        public static ReservedBalanceResult failed(String message) {
            return new ReservedBalanceResult(false, null, BigDecimal.ZERO, message, "RESERVE_FAILED");
        }
        
        public boolean isSuccess() { return success; }
        public String getHoldId() { return holdId; }
        public BigDecimal getAmount() { return amount; }
        public String getMessage() { return message; }
        public String getErrorCode() { return errorCode; }
    }
}
