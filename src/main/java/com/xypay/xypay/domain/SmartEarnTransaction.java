package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "smartearn_transactions")
public class SmartEarnTransaction extends BaseEntity {
    
    public enum TransactionType {
        DEPOSIT("deposit", "Deposit"),
        WITHDRAWAL("withdrawal", "Withdrawal"),
        INTEREST_CREDIT("interest_credit", "Interest Credit"),
        PROCESSING_FEE("processing_fee", "Processing Fee"),
        TRANSFER_IN("transfer_in", "Transfer In"),
        TRANSFER_OUT("transfer_out", "Transfer Out");
        
        private final String code;
        private final String description;
        
        TransactionType(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum TransactionStatus {
        PENDING("pending", "Pending"),
        CONFIRMED("confirmed", "Confirmed"),
        PROCESSING("processing", "Processing"),
        SUCCESS("success", "Success"),
        FAILED("failed", "Failed"),
        CANCELLED("cancelled", "Cancelled");
        
        private final String code;
        private final String description;
        
        TransactionStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "smartearn_account_id", nullable = false)
    private SmartEarnAccount smartEarnAccount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20, nullable = false)
    private TransactionType transactionType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "processing_fee", precision = 19, scale = 4, nullable = false)
    private BigDecimal processingFee = BigDecimal.ZERO;
    
    @Column(name = "balance_before", precision = 19, scale = 4, nullable = false)
    private BigDecimal balanceBefore;
    
    @Column(name = "balance_after", precision = 19, scale = 4, nullable = false)
    private BigDecimal balanceAfter;
    
    @Column(name = "reference", length = 50, unique = true, nullable = false)
    private String reference;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "transaction_time", nullable = false)
    private LocalDateTime transactionTime;
    
    @Column(name = "confirmation_date")
    private LocalDateTime confirmationDate;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @PrePersist
    protected void onCreate() {
        if (transactionTime == null) {
            transactionTime = LocalDateTime.now();
        }
    }
    
    /**
     * Check if this is a deposit transaction
     */
    public boolean isDeposit() {
        return TransactionType.DEPOSIT.equals(transactionType) || 
               TransactionType.TRANSFER_IN.equals(transactionType);
    }
    
    /**
     * Check if this is a withdrawal transaction
     */
    public boolean isWithdrawal() {
        return TransactionType.WITHDRAWAL.equals(transactionType) || 
               TransactionType.TRANSFER_OUT.equals(transactionType);
    }
    
    /**
     * Check if this is an interest credit transaction
     */
    public boolean isInterestCredit() {
        return TransactionType.INTEREST_CREDIT.equals(transactionType);
    }
    
    /**
     * Check if transaction is confirmed
     */
    public boolean isConfirmed() {
        return TransactionStatus.CONFIRMED.equals(status) || 
               TransactionStatus.SUCCESS.equals(status);
    }
    
    /**
     * Check if transaction is successful
     */
    public boolean isSuccessful() {
        return TransactionStatus.SUCCESS.equals(status);
    }
    
    /**
     * Get net amount (amount minus processing fee for deposits)
     */
    public BigDecimal getNetAmount() {
        if (isDeposit()) {
            return amount.subtract(processingFee);
        }
        return amount;
    }
}
