package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "spend_and_save_transactions")
public class SpendAndSaveTransaction {
    
    public enum TransactionType {
        AUTO_SAVE("auto_save", "Auto Save from Spending"),
        WITHDRAWAL("withdrawal", "Withdrawal"),
        INTEREST_CREDIT("interest_credit", "Interest Credit"),
        MANUAL_DEPOSIT("manual_deposit", "Manual Deposit"),
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
    
    public enum WithdrawalDestination {
        WALLET("wallet", "Wallet"),
        XYSAVE("xysave", "XySave Account");
        
        private final String code;
        private final String description;
        
        WithdrawalDestination(String code, String description) {
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
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spend_and_save_account_id", nullable = false)
    private SpendAndSaveAccount spendAndSaveAccount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20, nullable = false)
    private TransactionType transactionType;
    
    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "balance_before", precision = 19, scale = 4, nullable = false)
    private BigDecimal balanceBefore;
    
    @Column(name = "balance_after", precision = 19, scale = 4, nullable = false)
    private BigDecimal balanceAfter;
    
    @Column(name = "reference", length = 100, unique = true, nullable = false)
    private String reference;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "original_transaction_id")
    private UUID originalTransactionId;
    
    @Column(name = "original_transaction_amount", precision = 19, scale = 4)
    private BigDecimal originalTransactionAmount;
    
    @Column(name = "savings_percentage_applied", precision = 5, scale = 2)
    private BigDecimal savingsPercentageApplied;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "withdrawal_destination", length = 20)
    private WithdrawalDestination withdrawalDestination;
    
    @Column(name = "destination_account", length = 50)
    private String destinationAccount;
    
    @Column(name = "interest_earned", precision = 19, scale = 4, nullable = false)
    private BigDecimal interestEarned = BigDecimal.ZERO;
    
    @Column(name = "interest_breakdown", columnDefinition = "JSON")
    private String interestBreakdown = "{}";
    
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata = "{}";
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (reference == null) {
            reference = generateReference();
        }
    }
    
    private String generateReference() {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "SST" + uuid;
    }
}